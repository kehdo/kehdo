# CLAUDE.md — Backend Context

> Loaded **in addition to** `/CLAUDE.md`. Read the root file first.

---

## 🧱 Tech stack (locked)

| Layer | Choice | Version |
|-------|--------|---------|
| Language | Java | 21 LTS |
| Framework | Spring Boot | 3.2.x |
| Security | Spring Security + JWT (RS256) | 6.x |
| Database | PostgreSQL | 16 |
| ORM | Spring Data JPA + Hibernate | 6.x |
| Cache | Redis | 7 |
| Migrations | Flyway | 10.x |
| Build | Gradle Kotlin DSL | 8.5+ |
| Testing | JUnit 5 + Testcontainers | — |
| Observability | Micrometer + OpenTelemetry | — |
| Resilience | Resilience4j | — |

---

## 📂 Module structure (Gradle subprojects)

```
backend/
├── app/                  :app — executable entry point
├── api/                  :api — REST controllers, DTOs
├── auth/                 :auth — JWT, signup, login, refresh
├── user/                 :user — profile, quota, plan
├── conversation/         :conversation — upload, list, delete
├── ai/                   :ai — ★ OCR + LLM orchestration
│   ├── orchestrator/
│   ├── ocr/              (Google Vision adapter)
│   ├── speaker/          (2-stage attribution)
│   ├── llm/              (Vertex AI + OpenAI facade per ADR 0006)
│   ├── prompt/           (externalized templates)
│   └── safety/           (moderation)
├── infra/                :infra — rate-limit, S3, metrics
├── common/               :common — errors, utils
└── contracts-openapi/    :contracts-openapi — spec → Java stubs
```

**Dependency rules (enforced by Gradle):**
- `:api` depends on all feature modules
- Feature modules depend on `:common`, `:infra`, `:contracts-openapi`
- Feature modules **NEVER** import each other (e.g., `:ai` never imports `:user`)
- `:app` is the only place with `@SpringBootApplication`

---

## 🗃️ Database rules

- All schema changes via Flyway in `/src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V12__add_user_quota_reset_at.sql`)
- **Never edit a migration after merge** — write a new one
- UUID v7 for all primary keys (time-ordered, avoids enumeration)
- Soft deletes via `deleted_at` column; hard delete via nightly job after 30 days

---

## 🔐 Security defaults

- Passwords: BCrypt cost 12, never logged
- JWT access tokens: RS256, 5-minute TTL, stateless
- Refresh tokens: hashed in `sessions` table, 30-day TTL, rotate on use
- CORS: strict — only `kehdo.app`, `staging.kehdo.app`
- Rate limits: Redis token bucket, keyed by `user_id` (5/day free, 100/day paid)
- Request logging: only request IDs, never body content

---

## 🤖 AI pipeline rules

The `:ai` module is the product. **Phase 1 stack:** Vertex AI Gemini 2.0 Flash (primary) + OpenAI gpt-4o-mini (failover) + Google Cloud Vision (OCR). Full strategy + 5-phase migration plan in [ADR 0006](../docs/adrs/0006-ai-personalization-and-model-roadmap.md).

When touching the `:ai` module:

1. **Every LLM call goes through `LlmService`** — never call SDKs directly elsewhere
2. **Every prompt externalized** in `/src/main/resources/prompts/*.mustache`
3. **Token budgeting mandatory** via `TokenBudgeter` (hard limit: 3,000 tokens context)
4. **Circuit breaker mandatory** — Resilience4j on all external calls; gpt-4o-mini is the failover when Gemini fails
5. **Moderation mandatory** — every reply through `ModerationClient`
6. **Caching:** (conversation_hash + tone) cached in Redis 1 hour
7. **Model version tracked:** `model_used` column on every `replies` row
8. **Header-only OCR for contact name extraction.** When Contact Intelligence (Level 2 consent) is active, the vision prompt MUST instruct the model to extract the contact name from the chat header region only — never from message content. Enforce this in the prompt template and reject responses that include message-body text.
9. **Phone numbers must never be stored as `contact_profiles.contact_name`.** If header OCR returns a phone number, skip profile creation entirely.
10. **Three-layer prompt injection.** Layer 1 = app context (always). Layer 2 = global voice fingerprint (inject when global confidence ≥ 0.3). Layer 3 = contact-specific profile (inject when contact confidence ≥ 0.3 AND user has Level 2 consent for that contact). **Layer 3 overrides Layer 2 when they conflict.**
11. **Every reply emits an `interaction_signal` row** capturing the option shown vs. chosen, mode used, regen count, and app hint. This is the data flywheel — non-negotiable.
12. **Fine-tuning corpora are restricted.** Eligible: synthetic data, behavioral signals, and (with explicit Level-2+ consent) reply text the user actually selected. **Never eligible:** raw conversation text, OCR'd message bodies, screenshots. Privacy invariant — see [SECURITY.md](../SECURITY.md) and [privacy page](../web/src/app/privacy/page.tsx).

---

## 🧪 Testing standards

- **Controllers:** mock service layer, test request/response shapes only
- **Services:** test business logic with mocked repositories
- **Repositories:** integration tests with Testcontainers (real Postgres)
- **AI pipeline:** golden-test fixtures in `/src/test/resources/fixtures/screenshots/`
- **Coverage:** 80% on service layer, 60% overall

---

## 🏃 Running locally

```bash
# Postgres + Redis (from repo root)
cd infra
docker compose up -d              # Compose v2 syntax — note no hyphen

# Backend
cd ../backend
./gradlew :app:bootRun             # → http://localhost:8080/v1/health
./gradlew check                    # all tests
```

Smoke test the auth API via the Postman collection at
[`/docs/postman/`](../docs/postman/) or `curl.exe`:

```bash
curl.exe http://localhost:8080/v1/health
curl.exe -X POST http://localhost:8080/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"alex@example.com","password":"correct-horse-battery-staple"}'
```

(Live Swagger UI is not currently wired — the OpenAPI spec is the
canonical reference at `/contracts/openapi/kehdo.v1.yaml`.)

---

## 📦 Shipped status — through v0.5.0

Phase 2 (auth) shipped in `v0.3.0`. Phase 4 (AI pipeline + quota + storage)
shipped in `v0.5.0`. The following are LIVE behind `/v1/`:

| Endpoint | Method | Status |
|---|---|---|
| `/health` | GET | ✅ public liveness probe |
| `/auth/signup` | POST | ✅ creates STARTER user; rejects disposable-email domains with `422 EMAIL_DOMAIN_NOT_ALLOWED` |
| `/auth/login` | POST | ✅ same-shaped error for wrong password and unknown email (no info leak) |
| `/auth/refresh` | POST | ✅ rotates refresh token in place; same session id |
| `/auth/logout` | POST | ✅ requires Bearer JWT; revokes session via the `sid` claim |
| `/me` | GET | ✅ Phase 3.5 — returns the authenticated user's profile; 401 if the user has been soft-deleted since the access token was issued |
| `/me/usage` | GET | ✅ Phase 4 (v0.5.0) — Redis daily counter; returns `dailyUsed/dailyLimit/resetAt` (UTC midnight); 5/day STARTER, 100/day PRO, sentinel for UNLIMITED |
| `/tones` | GET | ✅ Phase 4 (v0.5.0) — full 18-tone catalog (8 free + 10 pro) seeded server-side with `isPro` flag |
| `/conversations` | POST | ✅ Phase 4 (v0.5.0) — reserves row in `PENDING_UPLOAD`; returns presigned S3 PUT URL (5-min TTL); MinIO in dev, S3 in prod |
| `/conversations/{id}/generate` | POST | ✅ Phase 4 (v0.5.0) — full pipeline: Cloud Vision OCR → heuristic speaker attribution → Vertex Gemini 2.0 Flash (primary) / OpenAI gpt-4o-mini (failover) → OpenAI omni-moderation; decrements quota; `402 DAILY_QUOTA_EXCEEDED` when over |
| `/replies/{id}/refine` | POST | ✅ Phase 4 (v0.5.0) — same LLM pipeline + quota counter as `/generate` |
| `/actuator/{health,info,metrics,prometheus}` | GET | ✅ public ops probes |

Implementation map:
- **`:common/error/`** — `ApiException` (typed; carries code + httpStatus +
  optional details map), `ErrorEnvelope` + `ApiError` records, `ErrorCode` constants
- **`:user/`** — `User` entity, `UserRepository` (active-only finders that
  exclude soft-deleted rows), `UserPlan` enum
- **`:auth/session/`** — `Session` entity (rotates in place; `revoke()` for
  logout), `SessionRepository` (`findByRefreshTokenHash` for O(1) refresh)
- **`:auth/jwt/`** — `JwtProperties`, `JwtKeys` (loads PEM from configured
  paths or falls back to ephemeral RSA-2048 in-memory keypair), `JwtService`
  (issue + validate, uses injected Clock so tests are deterministic)
- **`:auth/token/RefreshTokens`** — `generate()` returns `rt_<64hex>`,
  `hash()` SHA-256 hexes for DB storage; raw token only returned to client once
- **`:auth/validation/DisposableEmailValidator`** — loads
  `disposable-email-domains.txt` from classpath at startup (~250 entries);
  fail-fast if missing; case-insensitive domain match
- **`:auth/web/JwtAuthenticationFilter`** — `OncePerRequestFilter`; pulls
  `Authorization: Bearer <jwt>`; sets userId + sessionId on the request
  attribute (via constants `USER_ID_ATTRIBUTE` / `SESSION_ID_ATTRIBUTE`)
- **`:auth/service/AuthService`** — orchestrates signup, login, refresh, logout
- **`:api/auth/AuthController`** — REST surface; controllers always live in `:api/`
- **`:api/error/GlobalExceptionHandler`** — `@RestControllerAdvice` maps
  every exception (typed `ApiException`, validation, malformed JSON, Spring
  Security auth/access, 404, fallback `Exception`) → `ErrorEnvelope` JSON
- **`:app/config/SecurityConfig`** — wires JWT filter ahead of
  `UsernamePasswordAuthenticationFilter`; `/auth/{signup,login,refresh}`
  permitted, `/auth/logout` requires authentication
- **`:infra/logging/RequestLoggingFilter`** — logs path + status + duration;
  generates traceId in MDC; **never** reads request or response bodies

Schema in Flyway `V1__init_users_sessions.sql`:
- `users` (UUIDv7 id, email, BCrypt password_hash, plan, soft delete via
  `deleted_at`, unique-on-`LOWER(email)` only when active)
- `sessions` (UUIDv7 id, user_id FK, refresh_token_hash VARCHAR(64),
  expires_at, last_used_at, revoked_at; partial index for active sessions)

Tests in `:auth/src/test/java/`:
- `RefreshTokensTest` — format, length, determinism (5 tests)
- `JwtServiceTest` — issuance + validation, expiry, key mismatch, issuer
  mismatch, garbage input (5 tests)
- `AuthServiceTest` — happy paths + every error branch for all 4 flows
  (13 tests, including disposable-email block)
- `DisposableEmailValidatorTest` — blocklist load + lookup (7 tests)

**What's NOT yet implemented in the backend** (tracked in
[`/docs/BACKLOG.md`](../docs/BACKLOG.md)):
- `/auth/google` (Google Sign-In) — defined in OpenAPI, deferred per
  `contracts/CHANGELOG.md`
- Password reset, email confirmation — need email provider first
- 2FA / TOTP — post-launch
- Per-IP / per-user rate limiting on auth endpoints — Redis token bucket scaffolded but not wired
- `GET /conversations` (history page) and `GET/DELETE /conversations/{id}` —
  Phase 5 with the History feature module
- AWS deployment to `api.staging.kehdo.app` / `api.kehdo.app`

---

## 🚫 Do NOT

- Call any LLM SDK (Vertex AI, OpenAI, etc.) from outside `:ai/llm/`
- Create a `@RestController` outside `:api/`
- Inline SQL — use JPA or named queries
- Log request bodies or response bodies
- Add a top-level package without a Gradle submodule
- Return entities from controllers — always map to DTOs
- Modify a Flyway migration after commit

---

## ✅ Adding a new feature

1. Update OpenAPI spec in `/contracts/openapi/kehdo.v1.yaml` first
2. Run `./tools/generate-clients.sh`
3. Implement controller → service → repository in the right module
4. Write unit + integration tests
5. Manually test via Swagger UI
6. Update `/contracts/CHANGELOG.md` if breaking
7. Commit with conventional message
8. Ask Claude for the push command

---

*Backend context v1.0*
