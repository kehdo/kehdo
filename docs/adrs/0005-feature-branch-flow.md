# ADR-0005: Git Flow with develop intermediate branch

**Date:** 2026-04-22
**Status:** Accepted

## Context

We need a branching strategy that prevents untested code reaching production, supports multiple parallel features, allows human review and CI gates, and supports emergency hotfixes.

## Decision

Adopt **Git Flow**:

- **main** — production releases only, tagged
- **develop** — integration branch
- **feat/*** — feature branches off develop
- **fix/***, **chore/*** — bug fixes, maintenance off develop
- **hotfix/*** — emergency fixes off main, merged back to BOTH main AND develop

Merge flow: `feat/* → PR → develop` (CI + 1 review + manual QA), then `develop → PR → main` at release time.

## Consequences

### Enables
- main is always production-stable
- develop accumulates tested features
- Multiple features in parallel without blocking
- QA has a stable branch (develop) to test against
- Hotfixes ship without waiting for in-flight features

### Costs
- More complex than trunk-based development
- Discipline required: no direct pushes
- develop → main merge can surface hidden conflicts

### Mitigations
- Branch protection on GitHub enforces no direct pushes
- CODEOWNERS enforces reviewers for sensitive paths
- Claude has explicit rules to never push and never merge
- CI path-filtering keeps it fast

## Alternatives considered

- **Trunk-based development** — too risky for mobile (store approval cycles)
- **GitHub Flow** — too much trust in pre-merge testing for our context
