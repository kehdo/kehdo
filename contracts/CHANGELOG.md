# API Changelog

All notable changes to the kehdo API contract are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial OpenAPI 3.1 spec covering auth, user, conversation, reply, and tone endpoints
- Universal `ErrorEnvelope` schema for all 4xx/5xx responses
- 12 reply tones in `ToneCode` enum
- Canonical request/response example payloads for all four auth endpoints
  (`/auth/signup`, `/auth/login`, `/auth/refresh`, `/auth/logout`) under
  [`openapi/examples/auth/`](openapi/examples/auth/), including success
  cases and the documented 401/409 error envelopes — required by
  `contracts/CLAUDE.md`'s "every endpoint must have at least one example".

### Implementation status (Phase 2 backend)
- `/auth/signup`, `/auth/login`, `/auth/refresh`, `/auth/logout` — being
  implemented in the `feat/be/auth-*` PR series (Phase 2). Spec is locked;
  the backend implements against it without further contract changes.
- `/auth/google` — defined in spec but DEFERRED: social auth lands after
  email/password is solid (post-Phase 2). Backend will reject this path
  with `501 Not Implemented` until Phase 4.

## [1.0.0] — TBD

Initial release.
