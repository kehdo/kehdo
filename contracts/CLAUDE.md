# CLAUDE.md — Contracts Context

> Loaded **in addition to** `/CLAUDE.md`. This is the single source of truth for the API surface.

---

## 🎯 Purpose

Everything in `/contracts/` defines the agreement between backend and apps. **No endpoint exists in backend that isn't here first. No app calls an undefined endpoint.**

---

## 📂 Structure

```
contracts/
├── CLAUDE.md
├── CHANGELOG.md
├── openapi/
│   ├── kehdo.v1.yaml           ← the main spec
│   ├── schemas/                ← reusable JSON Schemas
│   └── examples/               ← canonical request/response bodies
├── events/                     ← AsyncAPI (push notifications, Phase 2)
└── errors/
    └── codes.yaml              ← canonical error codes + i18n keys
```

---

## 🔒 Rules — non-negotiable

### 1. Versioning

- **Path versioning:** all endpoints under `/v1/...`
- **No breaking changes within a version** — bump to `/v2/` if needed
- **Additive changes are safe:** new endpoint, new optional field, new response code
- **Breaking:** removing/renaming fields, changing types, optional→required, changing HTTP status

### 2. Change process

1. Open branch `feat/api/<description>`
2. Modify `kehdo.v1.yaml`
3. Run `./tools/validate-contracts.sh`
4. If breaking: update `CHANGELOG.md` with migration notes
5. PR requires **2 approvals** (one from `@api-reviewers`)
6. On merge, all 3 app CIs auto-regenerate clients

### 3. Naming

- **Paths:** `kebab-case`, plural nouns
  - ✅ `/conversations/{id}/replies`
  - ❌ `/getConversationReplies`
- **Schemas:** `PascalCase` (e.g., `GenerateRequest`, `ErrorEnvelope`)
- **Fields:** `camelCase` (e.g., `conversationId`, `createdAt`)
- **operationId:** `camelCase` verb phrases — these become method names
  - ✅ `generateReplies`, `getCurrentUser`
  - ❌ `generate_replies`, `GenerateReplies`

### 4. Every endpoint must have

- `operationId`
- `tags`
- `summary` (1 line)
- `description` (if non-obvious)
- `security` (`[]` for public, `[{bearerAuth: []}]` for authed)
- At least one example in `examples/`
- Explicit error responses

### 5. Universal error envelope

```yaml
ErrorEnvelope:
  type: object
  required: [error]
  properties:
    error:
      type: object
      required: [code, message]
      properties:
        code: { type: string }
        message: { type: string }
        traceId: { type: string }
        details:
          type: object
          additionalProperties: true
```

Codes defined in `errors/codes.yaml` with i18n keys. Never invent inline.

---

## 📦 Implementation status

This is the gap between what the spec defines and what the backend
currently implements. Spec drift is fine if a frontend can read the
contract and know "this isn't shipped yet."

| Path | Method | Spec | Backend | Notes |
|---|---|---|---|---|
| `/health` | GET | ✅ | ✅ | Phase 2 — liveness probe at `/v1/health` |
| `/auth/signup` | POST | ✅ | ✅ | Phase 2 — also rejects disposable-email domains (422 `EMAIL_DOMAIN_NOT_ALLOWED`) |
| `/auth/login` | POST | ✅ | ✅ | Phase 2 |
| `/auth/google` | POST | ✅ | ❌ | DEFERRED — backend will return `501 Not Implemented` until social auth ships post-Phase-2 |
| `/auth/refresh` | POST | ✅ | ✅ | Phase 2 — rotates refresh token in place |
| `/auth/logout` | POST | ✅ | ✅ | Phase 2 — requires Bearer JWT |
| `/me` | GET | ✅ | ✅ | Phase 3.5 — returns the authenticated user's `User` projection; `401 UNAUTHORIZED` when the access token's user is soft-deleted |
| `/me/usage` | GET | ✅ | ✅ | Phase 4 (v0.5.0) — Redis-backed daily counter, returns `dailyUsed/dailyLimit/resetAt` (UTC midnight). 5/day STARTER, 100/day PRO, sentinel for UNLIMITED |
| `/conversations` | POST | ✅ | ✅ | Phase 4 (v0.5.0) — reserves a row in `PENDING_UPLOAD`, returns presigned S3 PUT URL (5-min TTL). MinIO in dev, S3 in prod |
| `/conversations` | GET | ✅ | ✅ | Phase 5 — paginated history list, cursor-based (URL-safe base64 of `createdAt\|id`); `nextCursor: null` on last page |
| `/conversations/{id}` | GET/DELETE | ✅ | ✅ | Phase 5 — single conversation read (with parsed messages); soft delete via `deleted_at`. Hard-deleted by nightly cleanup after 30 days |
| `/conversations/{id}/generate` | POST | ✅ | ✅ | Phase 4 (v0.5.0) — full pipeline: Cloud Vision OCR → heuristic speaker attribution → Vertex AI Gemini 2.0 Flash (primary) / OpenAI gpt-4o-mini (failover) → OpenAI omni-moderation. Decrements daily quota; returns `402 DAILY_QUOTA_EXCEEDED` when over |
| `/replies/{id}/refine` | POST | ✅ | ✅ | Phase 4 (v0.5.0) — same LLM pipeline + quota counter as `/generate` |
| `/tones` | GET | ✅ | ✅ | Phase 4 (v0.5.0) — full 18-tone catalog (8 free + 10 pro) seeded server-side with `isPro` flag |

Keep this table accurate when endpoints flip from spec-only to implemented.
Out-of-date entries are worse than missing ones — an Android dev who reads
"❌ Not yet" knows to mock; "✅ Phase 2" they trust.

---

## 🚫 Do NOT

- Edit `kehdo.v1.yaml` without updating CHANGELOG for breaking changes
- Add a new endpoint without at least one example
- Add a new error code without adding it to `errors/codes.yaml`
- Use untyped `string` enums — list all values
- Return arbitrary JSON — always use a named schema
- Commit changes that fail `validate-contracts.sh`

---

## 🔄 Client generation

`./tools/generate-clients.sh` produces:

| Target | Generator | Output |
|--------|-----------|--------|
| Backend stubs | `spring` | `backend/contracts-openapi/.../generated/` |
| Android client | `kotlin` (Retrofit) | `android/core/core-network-generated/` |
| iOS client | `swift5` | `ios/Packages/KHNetwork/Sources/Generated/` |
| TypeScript (web/extensions) | `typescript-axios` | `web/src/generated/` |
| Docs site | `redoc` | `docs/api/index.html` |

**All generated code is gitignored.** Never edit, never commit.

---

## ✅ Adding a new endpoint

1. Draft request/response in `schemas/`
2. Add example(s) in `examples/`
3. Add path + operation in `kehdo.v1.yaml` with `$ref` to schemas
4. List all error codes in `responses`
5. Add new error codes to `errors/codes.yaml`
6. Run `./tools/validate-contracts.sh`
7. Open PR `feat/api/<endpoint-name>`
8. Wait for 2 approvals
9. After merge, backend implements, apps regenerate clients

---

*Contracts context v1.0*
