# CLAUDE.md — kehdo.app Root Context

> **Read this file first, every session, regardless of which directory is open.**
> Sub-directories have their own `CLAUDE.md` files that extend this one with platform-specific context.

---

## 🎯 Product identity

- **Name:** kehdo (कह दो — Hindi for "say it")
- **Domain:** kehdo.app
- **Tagline:** "Reply with quiet confidence."
- **What it does:** AI app that reads chat screenshots and generates contextual reply suggestions
- **Bundle/package IDs:**
  - Android: `app.kehdo.android`
  - iOS: `app.kehdo.ios`
  - Backend: `app.kehdo.backend`
- **Stores:** Google Play (`app.kehdo.android`), App Store (Phase 2)

> **Important:** the product was previously called "Flawlessly" during early planning. That name is **deprecated** and must not appear in any new code, docs, branding, or commit messages.

---

## 🎨 Brand — Aurora palette (locked)

```
Canvas (background):  #0A0612   /* Deep Violet Black */
Surface (cards):      #1A0F2E   /* Cosmic Surface */
Surface 2:            #24173D   /* Elevated Surface */
Signature purple:     #9C5BFF   /* Electric Purple */
Lighter purple:       #B47BFF   /* Lavender Glow */
Deeper purple:        #6B2FD9   /* Deep Amethyst */
Pink accent:          #EC4899   /* Electric Pink */
Amber accent:         #F59E0B   /* Solar Amber */
Blue support:         #3B82F6   /* Sapphire Blue */
Text primary:         #F5F3FF   /* Moonlight */
Success (status):     #10B981   /* Emerald */

Hero gradient:
  linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%)

Fonts:
  Primary:  Inter (400, 500, 600, 700, 800, 900)
  Accent:   Instrument Serif Italic (gradient headlines only)
```

**Rule:** All color and font values come from `/design/tokens/`. Never hardcode hex values in platform code. The token files generate native code on every build.

---

## 🏗️ Repository architecture

This is a **monorepo**. All platforms live in this single git repository on a single GitHub project: `<org>/kehdo`.

```
kehdo/
├── CLAUDE.md                  ← this file
├── contracts/                 ← OpenAPI spec = single source of truth
│   ├── CLAUDE.md
│   └── openapi/kehdo.v1.yaml
├── design/                    ← tokens, copy, assets
│   ├── CLAUDE.md
│   └── tokens/*.json
├── backend/                   ← Spring Boot 3.2 + Java 21
│   └── CLAUDE.md
├── android/                   ← Kotlin + Compose
│   └── CLAUDE.md
├── ios/                       ← Swift + SwiftUI
│   └── CLAUDE.md
├── web/                       ← Next.js 14 landing page
│   └── CLAUDE.md
├── extensions/                ← RESERVED — Phase 2
│   └── CLAUDE.md
├── infra/                     ← Terraform + K8s
├── tools/                     ← bootstrap, generators
└── docs/
    ├── master_plan.html
    ├── architecture.html
    ├── implementation.html
    ├── logo_brief.html
    └── adrs/
```

### Why monorepo

- Atomic commits across platforms for end-to-end features
- Single source of truth for API contracts and design tokens
- One place to bump API versions and track breaking changes
- Easier onboarding: one clone, one README, one CLAUDE.md hierarchy

---

## 🛡️ Git rules — CRITICAL — Claude MUST follow

### Claude's git permissions

- ❌ **Claude NEVER pushes code to any remote.** Ever. No exceptions.
- ❌ **Claude NEVER pushes to `main` or `develop`** — those branches are protected.
- ❌ **Claude NEVER runs `git push`, `git push --force`, or anything that writes to origin.**
- ❌ **Claude NEVER merges pull requests** — human-only action.
- ✅ **Claude MAY run local git operations:** `git status`, `git diff`, `git log`, `git branch`, `git checkout -b`, `git add`, `git commit`.
- ✅ **Claude provides the exact push command** for the user to run manually.
- ✅ **Claude generates PR descriptions** and commit messages in Conventional Commits format.

### Branch model — Git Flow

```
main          ← production releases only. Tagged (v1.0.0, v1.0.1, ...)
 ↑ merge with tag, manual approval
develop       ← integration. All feature branches merge here first.
 ↑ merge after PR + CI + manual QA
feat/*        ← feature branches — ALL work happens here
fix/*         ← bug fixes
chore/*       ← maintenance, deps, refactors
hotfix/*      ← emergencies — branch off main, merge back to main AND develop
```

### Branch naming

| Prefix | Example | Purpose |
|--------|---------|---------|
| `feat/be/` | `feat/be/jwt-refresh-endpoint` | Backend feature |
| `feat/and/` | `feat/and/login-screen` | Android feature |
| `feat/ios/` | `feat/ios/profile-screen` | iOS feature |
| `feat/web/` | `feat/web/landing-hero` | Landing page |
| `feat/api/` | `feat/api/add-refine-endpoint` | Contract change — triggers ALL app CI |
| `feat/design/` | `feat/design/tone-chip-spacing` | Design token change |
| `fix/<scope>/` | `fix/be/ocr-empty-result` | Bug fix |
| `chore/<scope>/` | `chore/and/bump-kotlin-1.9.22` | Maintenance |

### The merge flow (STRICT — no exceptions)

1. Create `feat/*` branch from `develop`
2. Claude writes code → user tests locally → user confirms working
3. Claude runs all tests → all green
4. Claude commits with conventional message
5. Claude gives user the push command (e.g., `git push origin feat/and/login-screen`)
6. User pushes manually
7. User opens PR to `develop` (using the auto-generated template)
8. CI runs → must pass
9. Human review → 1 approval minimum (2 for contract changes)
10. User merges to `develop`
11. QA on `develop`
12. When release-ready → user opens PR from `develop` to `main` → tagged release

### Merge rules — NON-NEGOTIABLE

- ❌ No merging without tests passing locally and in CI
- ❌ No merging without user confirmation (screenshots, manual QA, etc.)
- ❌ No direct merges to `main` — always via `develop`
- ❌ No "quick fixes" pushed directly — even one-line fixes follow the flow
- ❌ No merges on Fridays (avoids weekend on-call incidents)
- ✅ Hotfix exception: `hotfix/*` from `main`, merged back to BOTH `main` and `develop`

---

## 💬 Conventional Commits (enforced)

Format: `<type>(<scope>): <summary>`

```
feat(reply):     add refine-with-prompt flow on replies screen
fix(ai):         handle empty OCR result without crashing pipeline
refactor(auth):  extract JwtService to its own package
docs(adr):       record decision on monorepo strategy
chore(deps):     bump retrofit 2.9.0 → 2.11.0
test(convo):     cover soft-delete to hard-delete transition
perf(home):      lazy-load recent conversations carousel
style(ui):       fix button padding on login screen
build(ci):       add Trivy scan to backend workflow
```

Scope is the module/area, not the platform (branch name already says platform).

---

## 🧪 Testing rules — NON-NEGOTIABLE

- **Unit tests required** for every new use-case, service, or reducer
- **Integration tests required** for new API endpoints (backend) and new API calls (apps)
- **Snapshot tests required** for new UI components (Paparazzi on Android, swift-snapshot-testing on iOS)
- **Manual QA required** before merge — user confirms feature works on real device/browser
- **No merge without green CI** — exception: docs-only changes
- **Coverage minimum:** 80% on backend service layer, 60% overall

---

## 🔐 Security rules

- **Never commit secrets.** `.env*` files are gitignored. Use AWS Secrets Manager / GitHub Actions secrets.
- **Never log user content.** Conversation text, reply text, screenshot paths — all scrubbed from logs.
- **PII scrubbing** in Sentry + Datadog configured at project init.
- **API keys rotate quarterly** — OpenAI, Anthropic, Google Cloud, Firebase.
- **Certificate pinning** in mobile apps for `api.kehdo.app`.
- **TLS 1.3 only** — TLS 1.2 deprecated.

---

## 📝 Conventions across all platforms

### File naming

- **Kotlin/Swift classes:** `PascalCase.kt` / `PascalCase.swift`
- **Java classes:** `PascalCase.java`
- **Config files:** `kebab-case.yml` / `kebab-case.json`
- **Markdown:** `SCREAMING_CASE.md` for root docs, `kebab-case.md` for nested
- **Branch names:** `kebab-case` after the type prefix

### Test naming

- Kotlin: `ClassNameTest.kt` (unit), `ClassNameIntegrationTest.kt` (integration)
- Swift: `ClassNameTests.swift`
- Java: `ClassNameTest.java`
- Method naming: `should_<behavior>_when_<condition>()`

### Error envelope (universal)

Every API 4xx/5xx response across the entire system uses this exact shape:

```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Daily limit of 5 replies reached. Upgrade to continue.",
    "traceId": "01HXY7F..."
  }
}
```

Error codes are `UPPER_SNAKE_CASE`, defined in `/contracts/errors/codes.yaml`. Never invent inline.

---

## 🤝 How Claude works across directories

### When user opens `backend/` in IntelliJ

Claude loads: `/CLAUDE.md` + `/backend/CLAUDE.md`
→ Knows: universal rules + backend tech stack (Spring, Postgres, modules, AI rules)

### When user opens `android/` in Android Studio

Claude loads: `/CLAUDE.md` + `/android/CLAUDE.md`
→ Knows: universal rules + Android module structure, Compose patterns, MVI

### When user opens `ios/` in Xcode

Claude loads: `/CLAUDE.md` + `/ios/CLAUDE.md`
→ Knows: universal rules + Swift Package structure, SwiftUI patterns, MVVM

### When user opens `web/` in VS Code

Claude loads: `/CLAUDE.md` + `/web/CLAUDE.md`
→ Knows: universal rules + Next.js conventions, Tailwind setup, deployment

### When user asks Claude about another platform

Claude should remind: "I'm currently in the {current} context. For {other platform} work, open that directory in its IDE. I can answer high-level questions from the root CLAUDE.md, but platform-specific code should be written in the right context."

---

## 🚦 Decision checklist — when Claude must STOP and ask

If Claude is about to do any of these, **STOP and ask the user first:**

1. Add a new dependency (library, SDK, package)
2. Modify `/contracts/openapi/kehdo.v1.yaml`
3. Modify `/design/tokens/*.json`
4. Touch `.github/workflows/`
5. Run any push command (Claude should NEVER do this)
6. Delete existing code or files
7. Change a `CLAUDE.md` file
8. Create a new top-level directory
9. Bypass the feature branch flow for any reason
10. Commit when the user hasn't confirmed the feature works

---

## 📦 Current project status

- **Phase:** Scaffolding complete, implementation beginning
- **Active workstream:** Landing page (web)
- **Next up:** Backend auth → Android auth → AI pipeline → Android full flow → Android launch → iOS
- **Brand:** Aurora palette LOCKED
- **Product name:** kehdo.app (do NOT use "Flawlessly")
- **AI stack (Phase 1):** Vertex AI Gemini 2.0 Flash (primary) + OpenAI gpt-4o-mini (failover) + Google Cloud Vision (OCR). The landing page does NOT name the LLM — vendor flexibility is intentional.
- **AI personalization & roadmap:** voice fingerprint + contact intelligence with 3-layer prompt injection, 5-phase migration plan to a self-hosted model. See [ADR 0006](docs/adrs/0006-ai-personalization-and-model-roadmap.md).

---

## 🔗 Reference docs (in `/docs/`)

- `master_plan.html` — step-by-step implementation plan
- `architecture.html` — full technical architecture
- `implementation.html` — repo structure deep dive
- `logo_brief.html` — Aurora palette & logo specification
- `adrs/` — decision log

---

*Last updated: 2026-04-22 — initial scaffold*
