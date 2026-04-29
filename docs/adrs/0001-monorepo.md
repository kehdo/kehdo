# ADR-0001: Monorepo strategy

**Date:** 2026-04-22
**Status:** Accepted

## Context

kehdo.app has multiple platforms (backend, Android, iOS, web) with a shared API contract and shared brand. We need to decide between a single monorepo and multiple separate repositories.

## Decision

Use a **single git monorepo** containing all platforms, design tokens, infrastructure, and docs. Top-level folders: `backend/`, `android/`, `ios/`, `web/`, `contracts/`, `design/`, `extensions/`, `infra/`, `tools/`, `docs/`.

## Consequences

### Enables
- Atomic commits across platforms for end-to-end features
- Single source of truth for API contracts and design tokens
- Shared tooling and infrastructure
- One place to bump API versions and track breaking changes
- Easier onboarding: one clone, one README, one CLAUDE.md hierarchy

### Costs
- Larger clone size
- CI must be path-filtered to avoid rebuilding everything on every PR
- Each IDE must be scoped to its subdirectory

### Mitigations
- Path-filtered GitHub Actions ensure only affected platforms rebuild
- `.editorconfig` and sub-`CLAUDE.md` files keep each workstream self-contained
- Git LFS for large binary assets in `/design/assets/`

## References

- Google: "Why Google Stores Billions of Lines of Code in a Single Repository" (2016)
- Shopify, Airbnb, Stripe use the same pattern
