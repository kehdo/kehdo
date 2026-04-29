# ADR-0002: OpenAPI-first contract

**Date:** 2026-04-22
**Status:** Accepted

## Context

With three apps calling the same API, there's a high risk of drift: each platform implements an endpoint differently, bugs only surface in production integration. Fixing them requires multiple PRs.

## Decision

Adopt an **OpenAPI-first** workflow:

1. The OpenAPI 3.1 spec at `/contracts/openapi/kehdo.v1.yaml` is the single source of truth
2. All API changes start by editing this file
3. Clients for backend (Spring stubs), Android (Retrofit), iOS (Swift), web (TypeScript) are auto-generated
4. Generated code is in `.gitignore` and regenerated on every build
5. Breaking changes require 2 approvals and a CHANGELOG entry

## Consequences

### Enables
- Zero drift between platforms
- New clients cost a day of setup, not weeks
- Dedicated API reviewers catch breaking changes at PR time
- Great API documentation as a byproduct (Redoc at docs.kehdo.app)

### Costs
- Extra tooling: OpenAPI Generator, Spectral linter, oasdiff
- Learning curve for spec-first design
- Generated code can produce awkward signatures occasionally

### Mitigations
- Good schema design upfront (reusable components)
- Custom `operationId` values to produce clean method names
- `examples/` folder with canonical request/response bodies
