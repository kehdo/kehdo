# Postman collection — kehdo API

Drop-in collection for testing the kehdo backend by hand. Generated against
the OpenAPI spec at [`/contracts/openapi/kehdo.v1.yaml`](../../contracts/openapi/kehdo.v1.yaml);
keep this file in sync when endpoint shapes change.

## Files

- [`kehdo-api.postman_collection.json`](kehdo-api.postman_collection.json) —
  the collection itself: 1 health request, 4 happy-path auth requests, 6
  error-case requests, plus chained test scripts that auto-capture tokens.
- [`kehdo-api.local.postman_environment.json`](kehdo-api.local.postman_environment.json)
  — `baseUrl = http://localhost:8080/v1` for local Spring Boot bootRun.
- [`kehdo-api.staging.postman_environment.json`](kehdo-api.staging.postman_environment.json)
  — `baseUrl = https://api.staging.kehdo.app/v1` (placeholder until Phase 3+ deploy).
- [`kehdo-api.production.postman_environment.json`](kehdo-api.production.postman_environment.json)
  — `baseUrl = https://api.kehdo.app/v1` (same).

## Import into Postman

1. Open Postman → **File → Import** (or `Ctrl+O`)
2. Drag in all four JSON files at once, or click **Choose Files** and pick them
3. Click **Import**
4. In the top-right environment dropdown, select **kehdo — local**

## Recommended run order

| Step | Folder / Request | Why |
|---|---|---|
| 1 | **Health → GET /health** | Confirm the API is reachable. Expected 200 with `{ status:"ok", version, timestamp }`. |
| 2 | **Auth — happy path → 1. POST /auth/signup** | Creates a fresh account. Test script captures `accessToken` and `refreshToken` into collection variables. The email is uniquified per run via `{{$randomInt}}`. |
| 3 | **Auth — happy path → 2. POST /auth/login** | Sign back in with the original test account (email `alex@example.com` if you didn't change the body of step 2). Re-captures tokens. |
| 4 | **Auth — happy path → 3. POST /auth/refresh** | Rotates the refresh token. Test script verifies the new token is *different* from the old one (per SECURITY.md "rotate on every use"). |
| 5 | **Auth — happy path → 4. POST /auth/logout** | Revokes the session. Test script clears the stashed tokens. |

## Error-case folder

Run the requests under **Auth — error cases** in any order to verify the
contract for non-2xx responses. Each request has a test script asserting
the expected error code from
[`/contracts/errors/codes.yaml`](../../contracts/errors/codes.yaml):

- 400 BAD_REQUEST (validation fail)
- 401 INVALID_CREDENTIALS (wrong password / unknown email)
- 401 REFRESH_TOKEN_INVALID (stale refresh token)
- 401 UNAUTHORIZED (logout without auth)
- 409 EMAIL_ALREADY_REGISTERED (re-signup of an active account)

## Run the whole collection (Postman Runner)

Right-click the collection name → **Run collection** → pick the **kehdo —
local** environment → click **Run**. All happy-path requests run in order;
the test scripts assert response shapes; the error-case requests run after
and confirm rejection paths.

For headless runs (CI, scripts), use [Newman](https://www.npmjs.com/package/newman):

```bash
npm install -g newman
newman run docs/postman/kehdo-api.postman_collection.json \
  -e docs/postman/kehdo-api.local.postman_environment.json
```

## Keeping in sync with the OpenAPI contract

When `/contracts/openapi/kehdo.v1.yaml` changes:
1. Update the matching request body / response examples in this collection
2. Update test-script assertions if status codes or error codes shift
3. Bump the collection version note in `info.description` if it's a breaking change
