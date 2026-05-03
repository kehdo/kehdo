# Staging Deployment Runbook

This document is the one-time setup script for `api.staging.kehdo.app`. After it's done, every merge to `develop` auto-deploys via [.github/workflows/deploy-staging.yml](../.github/workflows/deploy-staging.yml). No further manual work.

> **Audience:** the maintainer (you) running through this once. Total time: ~45 minutes if you have all the cloud accounts ready.
>
> **Cost:** $0/month on the free tiers documented below — Fly.io shared-cpu-1x VM, Fly Postgres free shared cluster, Upstash Redis free tier (10K commands/day). The only paid line items are AWS S3 (pennies per month for the screenshot bucket) and your existing Vertex AI / OpenAI usage.

---

## 0. Accounts you need

Tick each before starting:

- [ ] **Fly.io** account with a billing card on file (free tier still requires one). Sign up at https://fly.io
- [ ] **AWS** account for the S3 screenshot bucket (existing one fine — we just need a new bucket)
- [ ] **Google Cloud** project with Vertex AI + Vision APIs enabled (you already set this up in Phase 4)
- [ ] **OpenAI** API key with at least the `gpt-4o-mini` and `omni-moderation-latest` models enabled
- [ ] **Upstash** account for Redis. Sign up at https://upstash.com
- [ ] **Domain** `kehdo.app` DNS access (Vercel or your registrar dashboard) — needed for the `staging.kehdo.app` CNAME
- [ ] **GitHub** repo admin access — needed to add the `FLY_API_TOKEN` secret

---

## 1. Generate the production JWT keypair

The backend's ephemeral RSA fallback only works for local dev — staging must have stable keys so tokens survive a redeploy.

```bash
mkdir -p ~/kehdo-secrets && cd ~/kehdo-secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl rsa -pubout -in jwt-private.pem -out jwt-public.pem

# Sanity check — both files should print BEGIN/END markers
head -1 jwt-private.pem  # -----BEGIN PRIVATE KEY-----
head -1 jwt-public.pem   # -----BEGIN PUBLIC KEY-----
```

> **Critical:** keep `jwt-private.pem` outside the repo. It's gitignored by [`backend/.dockerignore`](../backend/.dockerignore) but not by `.gitignore`. Don't `git add` it.

---

## 2. Provision Fly.io staging app

```bash
# Install flyctl if you don't have it
# macOS:    brew install flyctl
# Windows:  iwr https://fly.io/install.ps1 -useb | iex
# Linux:    curl -L https://fly.io/install.sh | sh

fly auth login                          # opens browser
cd backend

# Create the app (don't deploy yet — secrets first)
fly apps create kehdo-backend-staging

# Provision Postgres (free shared cluster)
fly postgres create \
  --name kehdo-backend-staging-db \
  --region bom \
  --vm-size shared-cpu-1x \
  --volume-size 1 \
  --initial-cluster-size 1
# At the prompt: pick "Development - Single node" / 256MB

# Attach Postgres to the app — this auto-injects DATABASE_URL,
# but Spring needs JDBC format. We also set the JDBC vars explicitly.
fly postgres attach kehdo-backend-staging-db --app kehdo-backend-staging
# Note the DATABASE_URL Fly prints — we'll convert it for SPRING_DATASOURCE_URL below.

# Create the persistent volume referenced in fly.staging.toml
fly volumes create kehdo_staging_data \
  --region bom \
  --size 1 \
  --app kehdo-backend-staging
```

---

## 3. Provision Upstash Redis

1. Go to https://console.upstash.com/redis
2. Create a database: name `kehdo-staging-cache`, region closest to `bom` (Mumbai), enable TLS
3. Copy the **Redis URL** from the *Connect* tab. It looks like `redis://default:<token>@<host>:6379`

---

## 4. Provision AWS S3 bucket

```bash
# Create the bucket (substitute your AWS profile if needed)
aws s3 mb s3://kehdo-screenshots-staging --region ap-south-1

# Block all public access (default but explicit is good)
aws s3api put-public-access-block \
  --bucket kehdo-screenshots-staging \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

# Server-side encryption with S3-managed keys
aws s3api put-bucket-encryption \
  --bucket kehdo-screenshots-staging \
  --server-side-encryption-configuration '{
    "Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]
  }'

# Lifecycle: hard-delete after 30 days (matches the soft-delete cleanup
# job's window). Adjust if your privacy policy says otherwise.
aws s3api put-bucket-lifecycle-configuration \
  --bucket kehdo-screenshots-staging \
  --lifecycle-configuration '{
    "Rules": [{
      "ID": "expire-after-30-days",
      "Status": "Enabled",
      "Filter": {"Prefix": ""},
      "Expiration": {"Days": 30}
    }]
  }'
```

Then create a dedicated IAM user for the backend (not your root key):

1. IAM Console → Users → Create user `kehdo-backend-staging`
2. Attach inline policy with `s3:GetObject`, `s3:PutObject`, `s3:DeleteObject` on `arn:aws:s3:::kehdo-screenshots-staging/*`
3. Create an access key for the user — copy the **Access Key ID** and **Secret Access Key**

---

## 5. Set Fly.io secrets

All of the values below are environment variables that become `${KEHDO_*}` lookups in [`application.yml`](../backend/app/src/main/resources/application.yml) and [`application-staging.yml`](../backend/app/src/main/resources/application-staging.yml).

```bash
cd backend  # so the --app flag is implicit from fly.staging.toml

fly secrets set --app kehdo-backend-staging \
  SPRING_DATASOURCE_URL="jdbc:postgresql://kehdo-backend-staging-db.flycast:5432/kehdo_backend_staging" \
  SPRING_DATASOURCE_USERNAME="<from fly postgres attach output>" \
  SPRING_DATASOURCE_PASSWORD="<from fly postgres attach output>" \
  REDIS_URL="<paste full Upstash redis://... URL>" \
  KEHDO_JWT_PUBLIC_KEY_PEM="$(cat ~/kehdo-secrets/jwt-public.pem)" \
  KEHDO_JWT_PRIVATE_KEY_PEM="$(cat ~/kehdo-secrets/jwt-private.pem)" \
  OPENAI_API_KEY="<your OpenAI key>" \
  VERTEX_PROJECT_ID="<your GCP project id>" \
  VERTEX_LOCATION="asia-south1" \
  GOOGLE_APPLICATION_CREDENTIALS="/data/gcp-credentials.json" \
  KEHDO_S3_REGION="ap-south-1" \
  KEHDO_S3_BUCKET="kehdo-screenshots-staging" \
  KEHDO_S3_ACCESS_KEY="<from step 4>" \
  KEHDO_S3_SECRET_KEY="<from step 4>"
```

The Google credentials JSON file needs to live on the persistent volume since `fly secrets` is for individual env vars, not files. Upload it after the first deploy:

```bash
fly machine list --app kehdo-backend-staging
# pick the machine ID, then:
fly ssh console --machine <id> --app kehdo-backend-staging
# inside the container:
cat > /data/gcp-credentials.json << 'EOF'
<paste the JSON content of your GCP service account key>
EOF
chmod 600 /data/gcp-credentials.json
exit
```

---

## 6. First deploy

```bash
cd backend
fly deploy --config fly.staging.toml --remote-only --wait-timeout 600
```

Watch the logs in another terminal:

```bash
fly logs --app kehdo-backend-staging
```

What you should see:
1. `Started Spring Boot in N seconds` (~30–60s on the free VM)
2. `JWT keys loaded from inline PEM (env-var-supplied)` — confirms the inline-PEM path activated, not the ephemeral fallback
3. Flyway migrations apply (`Successfully applied N migrations`)
4. `Tomcat started on port 8080`

Smoke-test:

```bash
curl https://kehdo-backend-staging.fly.dev/v1/health
# {"status":"UP"}

curl -X POST https://kehdo-backend-staging.fly.dev/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"smoke@example.com","password":"correct-horse-battery-staple"}'
# Returns access + refresh tokens
```

---

## 7. DNS for `staging.kehdo.app`

In your DNS provider (Vercel, Cloudflare, etc.):

```
Type   Name                  Value
CNAME  staging.kehdo.app     kehdo-backend-staging.fly.dev
       └─ alternative for apex-only providers: A record to Fly's IPs (`fly ips list`)
```

Then attach the cert in Fly:

```bash
fly certs add staging.kehdo.app --app kehdo-backend-staging
fly certs check staging.kehdo.app --app kehdo-backend-staging
# Wait for "verified" — usually <5 min
```

After verification, both URLs return identical responses:
- `https://kehdo-backend-staging.fly.dev/v1/health`
- `https://staging.kehdo.app/v1/health`

> Note: the actual hostname Android points at is `api.staging.kehdo.app` (preserves the `api.*` convention for prod). If you want that exact hostname, add a second CNAME `api.staging.kehdo.app → kehdo-backend-staging.fly.dev` and `fly certs add api.staging.kehdo.app`.

---

## 8. Wire CI/CD

```bash
# Generate a deploy token scoped only to this app
fly tokens create deploy --app kehdo-backend-staging --expiry 8760h
# Copy the long string this prints
```

In GitHub:
1. Repo Settings → Secrets and variables → Actions → New repository secret
2. Name: `FLY_API_TOKEN`
3. Value: paste the token
4. Save

From now on, every push to `develop` that touches `backend/` runs [.github/workflows/deploy-staging.yml](../.github/workflows/deploy-staging.yml), which:
- Checks out the code
- Installs flyctl
- Runs `fly deploy --config fly.staging.toml --remote-only --strategy rolling`
- Polls `/v1/health` until 200 (or fails the workflow)

---

## 9. Wire the Android app at the new URL

See [PR-5 next-up commit](#) — switches the debug `API_BASE_URL` from `http://10.0.2.2:8080/v1` to `https://api.staging.kehdo.app/v1` and flips `kehdo.useFakeData=false` as the new debug default.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| Deploy times out at "waiting for VM to start" | OOM: free shared-cpu-1x has 256MB | Check `fly logs` for `OutOfMemoryError`. Usually JVM heap; tweak `-XX:MaxRAMPercentage` in Dockerfile or scale up |
| Health check fails with 503 | Flyway migration fail | `fly logs` will show the SQL error. Most common: a bad `V*__*.sql` checksum from a hand-edit |
| `JWT keys generated EPHEMERAL` log line | Secrets not set | `fly secrets list --app kehdo-backend-staging` — confirm `KEHDO_JWT_*_PEM` are present |
| `403` from the S3 presigned URL | IAM user missing permissions | Re-check the policy in step 4 covers `s3:PutObject` on `bucket/*` |
| Slow first request after idle | Fly machine auto-stopped | We set `auto_stop_machines = false` in `fly.staging.toml` to avoid this; if it's happening, double-check that line wasn't reverted |
| `staging.kehdo.app` 404 / wrong cert | DNS hasn't propagated | `fly certs check` shows status; allow up to 30 min for cert issuance |

---

## What stays in BACKLOG (not done by this runbook)

- **AWS production tier** at `api.kehdo.app` — separate `fly.production.toml` (or a full ECS/Fargate setup) when budget allows
- **Cert pinning on Android** for production
- **Rotation runbook** for the JWT keypair (we generate once, no rotation drill yet)
- **Datadog / Sentry** wiring
- **Datadog APM / Prometheus scraping** — Fly's built-in metrics are enough for staging
- **Disaster recovery / PITR** for staging Postgres — we accept data loss on the free tier in exchange for $0/mo

When any of those graduates from BACKLOG to a real PR, this runbook gets a follow-up section.
