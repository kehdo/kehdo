# Git Workflow & Push Strategy

> **Answers the question: "Single repo or per-platform repos? How do we push?"**

This document is the definitive answer for kehdo. Everything below is enforced by branch protection rules and CODEOWNERS — read it once, reference it forever.

---

## TL;DR

- **One GitHub repository named `kehdo`** holds everything: backend, Android, iOS, web, contracts, design, infra, docs.
- **Two long-lived branches: `main` (production) and `develop` (integration).**
- **All work happens on feature branches** off `develop`.
- **Claude never pushes.** Claude commits locally, then gives you the exact `git push` command to run.
- **Nothing merges without testing + review.**

---

## Why one repo, not four

We considered four separate GitHub repos (one per platform) and rejected it. Here's why **monorepo wins for kehdo**:

| Concern | Multi-repo | Monorepo (chosen) |
|--------|-----------|-------------------|
| API contract changes | 3 separate PRs in 3 repos, easy to drift | 1 PR touches `/contracts/` and all clients |
| Design token updates | Copy-paste hex values into 3 repos | Edit `/design/tokens/`, all 3 platforms regenerate |
| End-to-end feature ("add refine button") | 3 PRs that must merge in correct order | 1 PR with backend + Android + iOS together |
| Onboarding new dev | "Which repos do I clone?" × 4 | `git clone kehdo` |
| CI bill | 4 sets of workflows to maintain | 1 set, path-filtered |
| Drift risk | High — apps lag backend silently | Low — one CI fails, everyone sees it |
| Repo size | Smaller per-repo | Larger (~50-200 MB at 1 year) |

The only thing multi-repo wins on is initial clone size. That's a one-time cost paid by maybe 20 humans over the life of the project. The drift cost is paid every day by every user.

> **Decision:** ONE repo on GitHub, named `kehdo`.
> Recorded in `/docs/adrs/0001-monorepo.md`.

---

## Initial setup — the only push you do yourself

Run these commands once, on your machine, after you've extracted the skeleton ZIP:

```bash
# 1. Enter the directory
cd kehdo

# 2. Initialize git (skip if already a repo)
git init
git branch -M main

# 3. First commit on main
git add .
git commit -m "chore: initial repo scaffolding with CLAUDE.md system

Establishes the kehdo monorepo with:
- Root + 7 sub-CLAUDE.md files for AI context propagation
- Backend (Spring Boot 3.2 + Java 21) — 9 Gradle submodules
- Android (Kotlin 1.9 + Compose) — 15 modules with convention plugins
- iOS (Swift 5.9 + SwiftUI) — 14 Swift Packages
- Web (Next.js 14) — landing page scaffold
- OpenAPI contract — 5 endpoint groups, 16 error codes
- Design tokens — Aurora palette + Inter typography
- 5 ADRs documenting key decisions
- CI workflows for all platforms (path-filtered)
- Conventional Commits + Git Flow + branch protection ready"

# 4. Create develop branch off main
git checkout -b develop

# 5. Add the GitHub remote
#    Replace <your-org> with your GitHub user or organization
git remote add origin git@github.com:<your-org>/kehdo.git

# 6. Push BOTH branches (this is the ONLY push you'll run yourself)
git push -u origin main
git push -u origin develop
```

**That's it for setup.** Every subsequent push is a feature branch.

---

## Configure GitHub branch protection (do this immediately)

Go to **Settings → Branches → Add rule** and create rules for both `main` and `develop`:

### `main` branch protection

- ✅ Require a pull request before merging
- ✅ Require approvals: **1 minimum**
- ✅ Require review from Code Owners
- ✅ Require status checks to pass before merging
  - Required: `backend-ci`, `android-ci`, `ios-ci`, `web-ci`, `contracts-ci`
- ✅ Require branches to be up to date before merging
- ✅ Require linear history
- ❌ Do not allow bypassing the above settings (even for admins)
- ❌ Allow force pushes — **DISABLED**
- ❌ Allow deletions — **DISABLED**

### `develop` branch protection

Same as `main` except:
- ✅ Require approvals: **1 minimum** (still 1, but reviewers can be from any team)
- ✅ Allow squash-merge only

### Bonus: protect `feat/api/*` branches

API contract changes affect all platforms. Add a CODEOWNERS rule (already in `.github/CODEOWNERS`):

```
/contracts/    @kehdo/api-reviewers @kehdo/engineering
```

This forces 2 approvals for any API change — one from API reviewers, one from engineering.

---

## Daily workflow — every feature, every fix

```
                    ┌──────────────────────────────────┐
                    │   You (or Claude) want to add    │
                    │   a new feature: "refine button" │
                    └──────────────┬───────────────────┘
                                   │
                                   ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 1. Create a feature branch off develop                    │
   │                                                           │
   │    git checkout develop                                   │
   │    git pull origin develop                                │
   │    git checkout -b feat/api/add-refine-endpoint           │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 2. Claude writes code, tests, runs them locally           │
   │                                                           │
   │    Claude:                                                │
   │    - Edits /contracts/openapi/kehdo.v1.yaml               │
   │    - Implements backend handler                           │
   │    - Writes unit + integration tests                      │
   │    - Runs ./gradlew check  → all green                    │
   │    - Stages files: git add ...                            │
   │    - Commits: git commit -m "feat(api): ..."              │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 3. YOU manually verify the feature works                  │
   │                                                           │
   │    - Curl the new endpoint locally                        │
   │    - Check Swagger UI at /swagger-ui.html                 │
   │    - Confirm tests still pass                             │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 4. Claude gives you the exact push command                │
   │                                                           │
   │    Claude says:                                           │
   │    "Run this to push and open a PR:"                      │
   │                                                           │
   │       git push -u origin feat/api/add-refine-endpoint     │
   │                                                           │
   │    YOU run it. Claude never does.                         │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 5. GitHub auto-opens PR, CI runs                          │
   │                                                           │
   │    - Backend CI: ✓                                        │
   │    - Android CI: ✓ (because contracts changed)            │
   │    - iOS CI: ✓ (because contracts changed)                │
   │    - Contracts CI: ✓ (lint + breaking-change check)       │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 6. Reviewer approves (you, or a teammate)                 │
   │                                                           │
   │    For /contracts/ changes: 2 approvals required          │
   │    (one from api-reviewers, one from engineering)         │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 7. YOU click "Squash and merge" → develop                 │
   │                                                           │
   │    Claude never merges. Human-only action.                │
   │    Branch auto-deleted after merge.                       │
   └───────────────────────┬───────────────────────────────────┘
                           │
                           ▼
   ┌───────────────────────────────────────────────────────────┐
   │ 8. develop auto-deploys to staging                        │
   │                                                           │
   │    api.staging.kehdo.app picks up new endpoint            │
   │    QA validates on staging                                │
   └───────────────────────────────────────────────────────────┘
```

When `develop` accumulates enough features for a release:

```
   ┌───────────────────────────────────────────────────────────┐
   │ 9. Open PR develop → main with release notes             │
   │ 10. After approval, merge to main                         │
   │ 11. Tag the release: git tag v1.2.0 && git push --tags    │
   │ 12. release.yml workflow ships to prod after approval     │
   └───────────────────────────────────────────────────────────┘
```

---

## Branch naming — locked

Format: `<type>/<scope>/<description>`

| Type | When |
|------|------|
| `feat/` | New feature |
| `fix/` | Bug fix |
| `chore/` | Refactor, deps, no user-visible change |
| `hotfix/` | Emergency fix off `main` |

Scope:

| Scope | Touches |
|-------|---------|
| `be/` | Backend only |
| `and/` | Android only |
| `ios/` | iOS only |
| `web/` | Web/landing page |
| `api/` | `/contracts/` — triggers CI for all 3 apps |
| `design/` | `/design/tokens/` — triggers all UI rebuilds |
| `infra/` | Terraform, K8s |
| `docs/` | Documentation only |

Description: kebab-case, descriptive but short.

**Examples:**
- ✅ `feat/api/add-refine-endpoint`
- ✅ `feat/and/swipe-between-replies`
- ✅ `fix/ios/keychain-token-not-persisted`
- ✅ `chore/web/bump-next-14.2`
- ❌ `feature-add-refine` — wrong format
- ❌ `feat/jonny-fixes-stuff` — not descriptive

---

## Conventional Commits — locked

Format: `<type>(<scope>): <summary>`

```
feat(reply):     add refine-with-prompt flow
fix(ai):         handle empty OCR result without crashing
refactor(auth):  extract JwtService to its own package
docs(adr):       record decision on monorepo
chore(deps):     bump retrofit 2.9.0 → 2.11.0
test(convo):     cover soft-delete transition
perf(home):      lazy-load recent conversations carousel
style(ui):       fix button padding on login screen
build(ci):       add Trivy scan to backend workflow
```

The scope is the **module/area**, not the platform — your branch already says the platform.

---

## Hotfix exception (the one detour)

When production is broken and we can't wait for a normal release:

```bash
# 1. Branch off main (NOT develop)
git checkout main
git pull origin main
git checkout -b hotfix/auth-token-validation-bug

# 2. Fix it, commit, push, PR → main
git push -u origin hotfix/auth-token-validation-bug

# 3. After merge to main + production deploy:
#    MERGE BACK TO DEVELOP TOO so the fix isn't lost
git checkout develop
git pull origin develop
git merge --no-ff main
git push origin develop
```

This is the only time you push to anything other than a feature branch — and even then, it's via PR.

---

## Friday rule

**No merges to `main` on Fridays.**

Standard ops hygiene. A bad Friday merge becomes a weekend on-call incident. Friday is for `feat/*` PRs against `develop` — not for production deploys.

Exception: critical hotfixes (security, data corruption). Everything else waits until Monday.

---

## What Claude can and can't do — recap

### ✅ Claude CAN

- `git status`, `git diff`, `git log`, `git branch` — read state
- `git checkout -b feat/...` — create feature branches
- `git add ...`, `git commit -m "..."` — local commits with conventional messages
- Run tests locally and report results
- Generate PR descriptions and commit messages
- Tell you the exact `git push` command to run

### ❌ Claude CANNOT (under any circumstances)

- `git push` — to any remote, ever
- Push to `main` or `develop` — even via feature branch shortcut
- Merge a PR — human-only action, after CI + review
- Force push (`git push --force`) — never
- Delete a branch on the remote — never
- Skip the feature-branch flow — no "quick fix" exception
- Add a dependency, modify contracts, or change CI without asking first

These rules are encoded in `/CLAUDE.md`. Every Claude session reads them before doing anything.

---

## Cheat sheet — copy-pasteable commands

```bash
# Start a new feature
git checkout develop && git pull
git checkout -b feat/<scope>/<description>

# After Claude writes code and you've tested
git push -u origin <branch-name>     # ← Claude tells you exactly what to type

# Check status of all branches
git branch -a

# Sync with develop while working on a long-lived feature
git checkout develop && git pull
git checkout <my-feature>
git rebase develop                    # or merge develop in if you prefer

# Tag a release (after develop → main merge)
git checkout main && git pull
git tag -a v1.2.0 -m "Release v1.2.0 — refine endpoint, paywall improvements"
git push origin v1.2.0

# Hotfix
git checkout main && git pull
git checkout -b hotfix/<description>
# ...fix, commit, push, PR → main
# After merge: also merge main → develop
```

---

*Last updated: 2026-04-29 — at repo creation*
