# Contributing to kehdo.app

> Read [`CLAUDE.md`](CLAUDE.md) first — it has the universal rules. This file expands on the day-to-day workflow.

---

## 🌿 Branch model — Git Flow

```
main      ← production releases only, tagged (v1.0.0, v1.0.1...)
 ↑
develop   ← integration branch
 ↑
feat/*    ← all features land here first
fix/*     ← all bug fixes
chore/*   ← maintenance, dep bumps, refactors
hotfix/*  ← production emergencies
```

**Branches you'll create:**

| Pattern | Example | Use for |
|---------|---------|---------|
| `feat/be/<name>` | `feat/be/jwt-refresh` | Backend feature |
| `feat/and/<name>` | `feat/and/login-screen` | Android feature |
| `feat/ios/<name>` | `feat/ios/profile-screen` | iOS feature |
| `feat/web/<name>` | `feat/web/landing-hero` | Web/landing page |
| `feat/api/<name>` | `feat/api/refine-endpoint` | Contract change (affects all apps) |
| `feat/design/<name>` | `feat/design/token-update` | Design token change |
| `feat/infra/<name>` | `feat/infra/staging-redis` | Terraform/K8s change |
| `fix/<scope>/<name>` | `fix/and/oom-on-large-image` | Bug fix |
| `chore/<scope>/<name>` | `chore/deps/kotlin-1.9.22` | Dependency or refactor |
| `hotfix/<name>` | `hotfix/production-500-on-login` | Production emergency |

---

## 📝 Commit messages — Conventional Commits

Format: `<type>(<scope>): <summary>` followed by an optional body.

```
feat(reply): add refine-with-prompt flow on replies screen

Adds a modal that lets users add free-text instructions to refine a
generated reply. Wires up POST /replies/{id}/refine and updates the
ReplyViewModel to handle the refinement state.

Closes #142
```

**Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `style`, `perf`, `build`, `ci`.

**Scope** is the module/area (e.g., `auth`, `ai`, `home`, `paywall`), not the platform.

---

## 🔄 The standard workflow

### 1. Pick up work from `develop`

```bash
git checkout develop
git pull origin develop
git checkout -b feat/and/login-screen
```

### 2. Write code with Claude

Open the relevant directory in your IDE. Claude reads the CLAUDE.md hierarchy automatically. Write code, run tests, iterate.

### 3. Test locally

```bash
# Android
cd android && ./gradlew check

# iOS
cd ios && xcodebuild test -scheme Kehdo -destination 'platform=iOS Simulator,name=iPhone 15'

# Backend
cd backend && ./gradlew check

# Web
cd web && pnpm test && pnpm build
```

**Manual QA is required.** Run the app on a real device/emulator/browser. Confirm the feature works.

### 4. Commit

```bash
git add .
git commit -m "feat(auth): add login screen with email + Google sign-in"
```

### 5. Push (Claude gives you the command)

```bash
git push origin feat/and/login-screen
```

### 6. Open a PR to `develop`

Use the GitHub PR template. Fill in:
- What changed
- Why
- Platforms touched
- Screenshots/recordings (required for UI changes)
- Testing checklist
- Breaking change marker

### 7. Wait for CI

GitHub Actions runs the relevant workflows based on which paths your PR touched. All must pass.

### 8. Get review

- 1 approval minimum
- 2 approvals for `feat/api/*` changes (one from `@api-reviewers`)
- 2 approvals for `feat/design/*` changes (one from `@design-reviewers`)

### 9. Merge to `develop`

Squash merge by default. Auto-delete the feature branch after merge.

### 10. Deploy

`develop` auto-deploys to staging environments (`staging.kehdo.app`, `api.staging.kehdo.app`).

---

## 🚀 Releases

When `develop` is ready for release:

```bash
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# Bump version numbers in:
#   - android/app/build.gradle.kts (versionCode + versionName)
#   - ios/App/Kehdo.xcodeproj (MARKETING_VERSION + CURRENT_PROJECT_VERSION)
#   - backend/build.gradle.kts (project.version)
#   - web/package.json (version)
#   - contracts/openapi/kehdo.v1.yaml (info.version)
#   - docs/CHANGELOG.md

git commit -am "chore(release): v1.2.0"
git push origin release/v1.2.0

# Open PR: release/v1.2.0 → main
# After merge:
git checkout main
git pull
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0

# Then sync develop:
git checkout develop
git merge main
git push origin develop
```

The `v1.2.0` tag triggers production deploy via GitHub Actions (with manual approval gate).

---

## 🚨 Hotfixes

Production has a critical bug. Bypass `develop`.

```bash
git checkout main
git pull
git checkout -b hotfix/login-500-error

# Fix it
git commit -am "fix(auth): handle null email in OAuth callback"
git push origin hotfix/login-500-error

# Open PR: hotfix/login-500-error → main
# After merge to main, tag v1.2.1, deploy to prod

# THEN merge the same fix into develop (do not skip this):
git checkout develop
git merge main
git push origin develop
```

---

## 🧪 Testing requirements

- **Unit tests** for every new use-case, service, or reducer
- **Integration tests** for every new API endpoint
- **Snapshot tests** for every new UI component
- **Manual QA** — user confirms the feature works before merge
- **CI must be green** — exception: docs-only changes

---

## 🔍 Code review checklist

Reviewers check:

- [ ] Code follows the conventions in the relevant `CLAUDE.md`
- [ ] Tests added or updated
- [ ] No hardcoded colors/strings/secrets
- [ ] No new dependencies without justification
- [ ] Conventional Commits format
- [ ] Screenshots provided for UI changes
- [ ] Breaking changes documented in CHANGELOG
- [ ] CLAUDE.md files updated if architecture changed

---

## 🧠 Working with AI assistants

This repo is built for AI-assisted development. Claude (and similar tools) read the `CLAUDE.md` hierarchy automatically.

**Best practices:**
- Open only the relevant workstream folder in your IDE (not the repo root)
- Trust the CLAUDE.md files — they encode our rules
- When Claude proposes a change that violates a rule, push back; the rules are there for a reason
- If a rule is wrong, change the CLAUDE.md file in a `chore/` PR — don't bypass it

**What Claude will NOT do:**
- Push to any remote
- Modify `main` or `develop` directly
- Merge pull requests
- Add dependencies without asking
- Modify contracts/design tokens without asking

---

## 🆘 Getting help

- **Architecture questions:** check `/docs/architecture.html` or open a discussion
- **Why is X done this way?** look in `/docs/adrs/`
- **CLAUDE.md unclear?** open a `chore/docs/clarify-x` PR with proposed changes
- **Stuck on a bug?** open a draft PR early; ask for help in the description

---

*Thanks for contributing to kehdo. Build boldly, ship rigorously.*
