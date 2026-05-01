# Auth example payloads

Canonical request and response bodies referenced by the auth endpoints in
[`../kehdo.v1.yaml`](../kehdo.v1.yaml). Per [`/contracts/CLAUDE.md`](../../CLAUDE.md):

> **Every endpoint must have at least one example in `examples/`.**

## Coverage

| Endpoint | Status | File |
|---|---|---|
| `POST /auth/signup` | `201` | [signup-response-201.json](signup-response-201.json) |
| `POST /auth/signup` | `409` | [signup-response-409.json](signup-response-409.json) — email already registered |
| `POST /auth/login` | `200` | [login-response-200.json](login-response-200.json) |
| `POST /auth/login` | `401` | [login-response-401.json](login-response-401.json) — invalid credentials |
| `POST /auth/refresh` | `200` | [refresh-response-200.json](refresh-response-200.json) — note rotated `refreshToken` |
| `POST /auth/refresh` | `401` | [refresh-response-401.json](refresh-response-401.json) — refresh token invalid |

Request bodies are paired with the same prefix:
[`signup-request.json`](signup-request.json),
[`login-request.json`](login-request.json),
[`refresh-request.json`](refresh-request.json).

`POST /auth/logout` takes no request body and returns `204 No Content`, so it
needs no example file.

## Notes

- `accessToken` examples are syntactically valid (header.payload.signature)
  but use a placeholder signature segment; do not trust them for any actual
  verification.
- `refreshToken` examples use the `rt_<64 hex chars>` shape that matches the
  format issued by the backend.
- `refresh-response-200.json` shows token rotation: every successful refresh
  issues a NEW `refreshToken` and invalidates the previous one (per
  [`/SECURITY.md`](../../../SECURITY.md): "Refresh tokens are hashed in the
  database and rotate on every use").
- `traceId` values are illustrative. The backend populates them from the
  current OpenTelemetry/Sleuth trace.
- User `id` values are UUID v7 (time-ordered) per [`backend/CLAUDE.md`](../../../backend/CLAUDE.md).
