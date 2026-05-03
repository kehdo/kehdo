# kehdo backlog — deferred work

Things we know we want or need but have intentionally deferred to keep
each phase scoped. Updated as items move from "deferred" → "in progress" → "done".

> **Discoverability:** linked from the root `/CLAUDE.md` and from each
> platform's `CLAUDE.md` so future Claude sessions can find it.

## 🔐 Auth & security — disadvantages of choosing backend-driven auth (Option A)

We chose to own auth ourselves rather than use Firebase Auth. The
upside is privacy positioning + zero vendor lock-in. The downside is
that everything Firebase gives us for free is on this list. None are
urgent for closed beta; all are in scope before public launch.

### Required before public launch
- [ ] **Password reset flow** — `POST /v1/auth/forgot-password` + `/v1/auth/reset-password` with time-limited reset tokens. **Blocker:** needs an email provider (Resend / AWS SES / Mailgun).
- [ ] **Email confirmation on signup** — verification link sent post-signup; account un-verified state until clicked. Same email-provider blocker.
- [ ] **Rate limiting on auth endpoints** — Redis token bucket (~5 attempts per IP per 60s on `/auth/login` and `/auth/signup`). Scaffolded in `application.yml` but not wired.
- [ ] **Account lockout after N failed login attempts** — e.g., 5 wrong passwords in 15 min → 30-min lock. Audit log entry on each lockout.
- [ ] **JWT keypair rotation runbook** — Phase 5 ships an inline-PEM-from-secrets path (`JwtKeys` reads `KEHDO_JWT_*_PEM` first, then files, then ephemeral). Production needs a documented rotation drill: generate new pair → set new secret → restart with both old and new public keys trusted briefly → cut over → drop old. Currently the staging key is generated once in `STAGING_DEPLOYMENT.md` step 1 and never rotated.

### Nice-to-have before public launch
- [ ] **Google Sign-In** — `/v1/auth/google` is in the OpenAPI spec but stubbed; backend currently 501s. Needs GCP OAuth client + `google-auth-library` + account-linking flow (what if a user signed up with email then later logs in via Google with the same email?).
- [ ] **Apple Sign-In** — `/v1/auth/apple` for iOS launch. Apple Developer Program required ($99/yr).

### Post-launch
- [ ] **MFA / TOTP** — TOTP setup with QR provisioning, recovery codes, verification step on login. Probably ~3-4 days.
- [ ] **Anonymous "try before you sign up"** — let users hit `/v1/conversations` with an anonymous session ID, capped quota. Significant scope; only build if onboarding metrics demand it.
- [ ] **Phone auth** — probably never. Keep on radar.
- [ ] **Session management UI** — "List my devices" + "Log out all devices". Backend route already exists (`SessionRepository.revokeAllActiveByUserId`), no UI.
- [ ] **Account deletion flow** — GDPR Article 17 right to erasure. Two-step (request + confirm) with 30-day cooldown.
- [ ] **Data export** — GDPR Article 20 right to data portability. JSON / CSV download of every row tied to the user.

## 🚀 Infrastructure / deploy

**Staging (DONE in Phase 5):** `api.staging.kehdo.app` runs on Fly.io
free tier — see [/docs/STAGING_DEPLOYMENT.md](STAGING_DEPLOYMENT.md). CI auto-deploys
on every merge to `develop` via [`.github/workflows/deploy-staging.yml`](../.github/workflows/deploy-staging.yml).

Still deferred:
- [ ] **Production tier** at `api.kehdo.app` — separate `fly.production.toml` (or graduate to AWS ECS/Fargate when budget allows; ~$30-40/mo with RDS Postgres `db.t4g.micro` + ElastiCache Redis)
- [ ] **Terraform** for the production infra (Fly's CLI is fine for staging)
- [ ] **Postgres backup + PITR** strategy (Fly free Postgres has none; production tier needs RDS auto-backups + manual snapshot pre-migration runbook)
- [ ] **Datadog / Sentry** — error tracking + APM. Sentry's free tier covers 5K events/mo. Currently relying on `fly logs` for staging.
- [ ] **Cert pinning on Android** for production builds against `api.kehdo.app` — `:core:network`'s OkHttp config already has the TODO.

## 🤖 AI pipeline — Phase 4

Implements ADR 0006's 5-phase roadmap. Inside `:ai/`:

- [ ] `:ai/orchestrator/` — request → OCR → speaker attribution → LLM → moderation → response
- [ ] `:ai/ocr/` — Google Cloud Vision adapter (header-only contact-name extraction per ADR 0006 rule)
- [ ] `:ai/speaker/` — 2-stage attribution (heuristic + LLM fallback)
- [ ] `:ai/llm/` — Vertex AI Gemini 2.0 Flash primary + OpenAI gpt-4o-mini failover; circuit breaker via Resilience4j; token budget cap 3000
- [ ] `:ai/prompt/` — externalized Mustache templates; 3-layer injection (app context / global voice fingerprint / contact-specific profile)
- [ ] `:ai/safety/` — moderation client; block reasons: child safety, PII, weapons, etc.
- [ ] `interaction_signal` table + emission on every reply
- [ ] Voice fingerprint capture flow (default-OFF, opt-in)
- [ ] Contact intelligence layer (default-OFF, two-step Level-2 consent)
- [ ] Mode × tone matrix (4 modes × 18 tones = 72 prompt configurations)

## 🛠 Backend chores

- [ ] **Testcontainers integration tests** for `/v1/auth/*` — deferred from Phase 2 PR 5 (needs Docker on the build machine, which CI doesn't always have)
- [ ] **Drop the obsolete `version:` field** from `/infra/docker-compose.yml` — emits a deprecation warning on every Compose v2 startup
- [ ] **Wire springdoc-openapi** for live Swagger UI at `/v1/swagger-ui.html` — currently the OpenAPI spec is the only canonical reference
- [ ] **Refactor module `build.gradle.kts` files to use `gradle/libs.versions.toml`** — version pins are inline today; backend/CLAUDE.md says "All versions live in `gradle/libs.versions.toml`" but the rule isn't enforced yet
- [ ] **Quarterly refresh** of `disposable-email-domains.txt` — sync from upstream `github.com/disposable-email-domains/disposable-email-domains`. Set a calendar reminder.

## 📱 Web — Phase 1.5+ deferrals

- [ ] **Real testimonials** replacing the anonymized "Closed-beta tester" placeholders in `web/src/components/Testimonials.tsx`
- [ ] **Twitter / X handle** in Footer when registered
- [ ] **Logo SVG** to replace the wordmark text in `web/src/components/{Nav,Footer}.tsx`
- [ ] **Lawyer review** of `/privacy` and `/terms` before public launch — pre-incorporation language in privacy policy needs updating once the legal entity is filed
- [ ] **Hindi / Spanish / Portuguese translations** of the landing page (`design/copy/{hi,es,pt}.json` exist but the web layer doesn't render them yet — `next-intl` setup needed)
- [ ] **Add `/api/waitlist`-equivalent landing-side rate limiting** to prevent the Apps Script webhook from being hammered

## 💳 Paywall / billing — Phase 6 (post-staging-validation)

`:feature:paywall` and the backend `/billing/*` surface are out of scope
for Phase 5; we want to validate the core conversation loop on staging
with the free tier first.

- [ ] **Play Billing client** in `:feature:paywall` — purchase flow, restore purchases, receipt validation
- [ ] **Backend `/billing/play-purchase`** — verify Play receipt server-side, flip user plan to PRO/UNLIMITED
- [ ] **`/billing/cancel`** + downgrade-on-period-end logic
- [ ] **Apple In-App Purchase** equivalent — when iOS launches

## 📱 Android — Phase 3 follow-ups (post-scaffold)

To be expanded as Phase 3 progresses; these are known gaps that won't all
fit in the initial scaffold PRs:

- [ ] **Generate Retrofit client** from `/contracts/openapi/kehdo.v1.yaml` via `tools/generate-clients.sh` — currently hand-rolled `ConversationApi.kt` + `AuthApi.kt`
- [ ] **Generate design tokens** for Android via `tools/generate-tokens.sh` → `core-ui/AuroraColors.kt`
- [ ] **Aurora theme** wired into `:core:ui` — screens currently reach `AuroraColors.*` directly with no `KehdoTheme {}` wrapper
- [ ] **Paparazzi snapshot tests** for `:core:ui` components — Android `CLAUDE.md` mandates them but Phase 4/5 PRs didn't add any
- [ ] **Firebase Test Lab** wiring in CI (Pixel 6 + Pixel 4 + Samsung A13)
- [ ] **Account deletion** flow on the Profile screen — currently shown as "coming soon"; needs GDPR Article 17 confirm + 30-day cooldown

## 📱 iOS — Phase 6+ scope

Effectively empty until Android beta is live. Will mirror Android.

- [ ] Full SwiftUI scaffold matching `ios/CLAUDE.md`
- [ ] `KHNetwork` package with the same auth flow as Android
- [ ] App Store submission

## 🧩 Extensions — Phase 7+ (Q3+)

Per `extensions/CLAUDE.md`. Each is a thin shell over the core apps.

- [ ] Android Share Intent — highest value, lowest risk
- [ ] iOS Share Sheet
- [ ] Chrome / Edge browser extension
- [ ] Android Keyboard (IME)
- [ ] iOS Keyboard Extension

---

*Maintainer convention: when an item ships, move it OUT of this file (don't just check it). The list should always be the live "deferred" set, not a history.*
