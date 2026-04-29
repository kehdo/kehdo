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
│   ├── llm/              (OpenAI + Anthropic facade)
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

The `:ai` module is the product. When touching it:

1. **Every LLM call goes through `LlmService`** — never call SDKs directly elsewhere
2. **Every prompt externalized** in `/src/main/resources/prompts/*.mustache`
3. **Token budgeting mandatory** via `TokenBudgeter` (hard limit: 3,000 tokens context)
4. **Circuit breaker mandatory** — Resilience4j on all external calls
5. **Moderation mandatory** — every reply through `ModerationClient`
6. **Caching:** (conversation_hash + tone) cached in Redis 1 hour
7. **Model version tracked:** `model_used` column on every `replies` row

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
cd backend
docker-compose up -d              # postgres + redis + minio
./gradlew :app:bootRun             # http://localhost:8080
./gradlew check                    # all tests
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🚫 Do NOT

- Call OpenAI/Anthropic SDKs from outside `:ai/llm/`
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
