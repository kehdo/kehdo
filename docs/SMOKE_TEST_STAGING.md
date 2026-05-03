# Staging Smoke Test

After [STAGING_DEPLOYMENT.md](STAGING_DEPLOYMENT.md) finishes and PR-5 (this PR) merges, this is the runbook to confirm the whole stack — backend on Fly.io + Android app pointing at it — actually works end-to-end.

> **When to run:** once after the first staging deploy, then any time CI deploys a backend change you're nervous about, then before any release tag.
>
> **Time:** ~10 minutes if everything works, longer if you find drift.

---

## 0. Prerequisites

- [ ] PR-4 deployed: `https://api.staging.kehdo.app/v1/health` returns `{"status":"UP"}`
- [ ] PR-5 merged: `:core:network`'s debug `API_BASE_URL` defaults to `https://api.staging.kehdo.app/v1/`
- [ ] Android emulator OR physical device with internet — `adb reverse` is no longer needed since we're going through public DNS
- [ ] (Optional) `curl` / `httpie` installed for raw API spot-checks

---

## 1. Backend health (~2 min)

```bash
# Liveness probe
curl https://api.staging.kehdo.app/v1/health
# expected: {"status":"UP"}

# Tones — proves the database + Hibernate + Flyway came up cleanly
curl https://api.staging.kehdo.app/v1/tones
# expected: array of 18 tones, mix of isPro=true/false

# Round-trip auth — proves Postgres + bcrypt + JWT signing chain
curl -X POST https://api.staging.kehdo.app/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"smoke-'"$(date +%s)"'@example.com","password":"correct-horse-battery-staple"}'
# expected: { accessToken, refreshToken, expiresIn: 300, user: { ... } }

# Save the token from above:
TOKEN="<paste accessToken>"

# /me/usage — proves Redis is reachable + quota counter wired
curl -H "Authorization: Bearer $TOKEN" https://api.staging.kehdo.app/v1/me/usage
# expected: { dailyUsed: 0, dailyLimit: 5, resetAt: "<UTC midnight ISO>" }
```

If any of these fail, jump to [Troubleshooting](#troubleshooting). Don't continue with the Android run — fixing backend drift is faster from a terminal than through a UI.

---

## 2. Android build with staging URL (~3 min)

```bash
cd android
./gradlew --stop                 # clear any stale daemons

# Verify the URL the build will bake in
./gradlew :core:network:assembleDebug --console=plain | grep API_BASE_URL || true
# (no output is fine — just confirming nothing errors)

./gradlew :app:installDebug
```

Expected: install succeeds. If you want to confirm the URL is what we think it is, decompile the BuildConfig with `apkanalyzer` or just open the generated `core-network/build/.../BuildConfig.java` — `API_BASE_URL` should be `"https://api.staging.kehdo.app/v1/"`.

If you intentionally want to test a different URL (e.g., your local backend) for this run only:

```bash
./gradlew :app:installDebug -Pkehdo.apiBaseUrl=http://10.0.2.2:8080/v1/ -Pkehdo.useFakeData=false
```

---

## 3. End-to-end flow (~5 min)

Open the app on the emulator or device. Walk through:

| Step | Action | Expected |
|---|---|---|
| 1 | Tap **Sign up** with a fresh email + password | Lands on Home; quota footer not yet visible there |
| 2 | Tap **Profile** | Email + plan = `Starter (free)` + quota bar at `5 of 5 left today` |
| 3 | Tap **Back**, then **New reply** | Upload screen with Mode picker |
| 4 | Pick a screenshot from the photo picker | Image preview renders |
| 5 | Tap a Mode (e.g. **Casual**) → tap a Tone (e.g. **Casual**) | Generate button enables |
| 6 | Tap **Generate replies** | Spinner ~3-8s, then bounces to Reply screen with 3-4 ranked suggestions |
| 7 | Tap **Copy** on one reply | Pill flips to "Copied"; clipboard contains the text (long-press anywhere to verify) |
| 8 | Tap **Save** on a different reply | Pill flips to "★ Saved" |
| 9 | Tap **Done** → land on Home → tap **History** | One row visible with the tone you picked + timestamp |
| 10 | Tap the row | Re-enters Reply screen; the saved/copied state is preserved |
| 11 | Back to History → tap **Delete** | Row disappears immediately (optimistic) |
| 12 | Tap **Profile** again | Quota bar now shows `4 of 5 left today` (decremented by the generate call) |

**Hard fails to watch for:**
- Any HTTP 500 → check `fly logs --app kehdo-backend-staging`
- HTTP 401 immediately after sign-up → JWT keypair mismatch, see Troubleshooting
- HTTP 422 `CONTENT_BLOCKED` on every screenshot → moderation provider is too aggressive; lower priority but log a fix-it
- Generate spins forever → most likely a Vertex/OpenAI auth issue; `fly logs` will show the upstream error

---

## 4. Pass / fail report

Either:
- **All 12 steps green** → tag this commit as passing the staging smoke; no follow-ups needed.
- **N steps failed** → file a fix-it commit per failure, citing the step number. The Phase 5 close-out tag (`v0.6.0`?) waits until all 12 are green.

Document the result in the PR description or as an issue comment so it's findable later.

---

## Troubleshooting

| Symptom | Likely cause | Where to look |
|---|---|---|
| `/v1/health` returns 502 / 504 | VM still booting (cold start ~60s) | `fly logs --app kehdo-backend-staging` |
| `/v1/tones` returns empty array | Tones never seeded | Check Flyway ran the seed migration; see backend logs for `V*__seed_tones.sql` |
| Sign-up returns 401 / 403 | Disposable email block (we ship a 250-domain blocklist) | Use a real domain like `example.com` |
| `/me/usage` returns 500 | Redis unreachable | `fly logs` for `RedisConnectionException`; verify `REDIS_URL` is set |
| Android build fails with "cleartext not allowed" | Pointing at `http://...` URL but `network_security_config` wasn't extended | The default staging URL is HTTPS; if you really need plaintext, edit `network_security_config.xml` |
| Generate hangs > 30s then 502 | Vertex AI credentials missing or wrong project | `fly logs` for `PERMISSION_DENIED` or `UNAUTHENTICATED`; re-set `GOOGLE_APPLICATION_CREDENTIALS` |
| 18 tones but the Android picker shows 0 | Tone code <-> Mode mapping in `ConversationMappers.modeFor` doesn't match the seed | Inspect the response: `curl /v1/tones`, then check each code maps in `:data:conversation/mapper/ConversationMappers.kt` |
| Reply screen shows "Working on it…" forever | Generate succeeded but the ViewModel's local cache update raced the Flow | Reproduce + capture `adb logcat -s System.out`. Known sharp edge — file a fix-it with the trace |

---

*Smoke test runbook v1.0 — Phase 5, Fly.io staging on `api.staging.kehdo.app`.*
