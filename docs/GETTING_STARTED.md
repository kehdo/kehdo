# Getting Started with kehdo

> Welcome. This is the orientation guide. **Read this first.** Every other doc references it.

---

## What is kehdo?

**kehdo.app** is an AI-powered chat reply generator. Users drop a screenshot of a chat conversation (WhatsApp, iMessage, Slack, anywhere) and get four ranked, contextual reply suggestions in their chosen tone. The name means "say it" in Hindi (कह दो).

- **Free tier:** 5 replies/day, forever, no ads
- **Pro:** $4/mo billed yearly — 100 replies/day
- **Unlimited:** $12/mo — unmetered
- **Privacy-first:** screenshots auto-delete after 30 days; zero training on user data

---

## Repository layout — the 30-second tour

```
kehdo/
├── CLAUDE.md              ← Universal AI/dev context. READ FIRST.
├── README.md
├── CONTRIBUTING.md        ← How to make changes
├── docs/
│   ├── GETTING_STARTED.md ← THIS FILE
│   ├── GIT_WORKFLOW.md    ← Branching, push strategy, the rules
│   ├── master_plan.html   ← Big-picture roadmap
│   ├── architecture.html  ← Technical architecture (16 sections)
│   └── adrs/              ← Architecture Decision Records
│
├── contracts/             ← OpenAPI spec — single source of truth for the API
├── design/                ← Aurora palette, typography, copy (i18n)
│
├── backend/               ← Spring Boot 3.2 + Java 21 + PostgreSQL
├── android/               ← Kotlin 1.9 + Jetpack Compose (15 modules)
├── ios/                   ← Swift 5.9 + SwiftUI (14 packages)
├── web/                   ← Next.js 14 — landing page at kehdo.app
│
├── extensions/            ← RESERVED for Phase 2 (Chrome, Share, Keyboard)
├── infra/                 ← Terraform + K8s + docker-compose
├── tools/                 ← Scripts: bootstrap, generate-clients, generate-tokens
└── .github/workflows/     ← Path-filtered CI for each platform
```

Every workstream folder has its own `CLAUDE.md` with platform-specific rules. When you open `android/` in Android Studio, Claude reads root `CLAUDE.md` + `android/CLAUDE.md` automatically — no setup required.

---

## The four kinds of changes

Every change you'll ever make falls into one of these four buckets. Knowing which one you're in tells you which folder to start in:

| Change type | Start in | Affects |
|-------------|----------|---------|
| **API change** | `/contracts/openapi/kehdo.v1.yaml` | Backend + Android + iOS clients regenerate |
| **Visual change** | `/design/tokens/*.json` | Both apps + landing page rebuild from same source |
| **Business logic** | `/backend/` | Backend only — apps unchanged |
| **Platform UX** | `/android/` or `/ios/` or `/web/` | That platform only |

---

## Day 1 — your first hour

```bash
# 1. Clone (after you push the skeleton — see GIT_WORKFLOW.md)
git clone git@github.com:<your-org>/kehdo.git
cd kehdo

# 2. Read these three files in order (~20 min total)
cat CLAUDE.md                     # universal context
cat docs/GIT_WORKFLOW.md          # branch model, push rules
cat docs/architecture.html        # open in a browser

# 3. Bootstrap (one command, ~10 minutes)
./tools/bootstrap.sh
# ↳ verifies tools (java, node, gradle)
# ↳ generates design tokens for all platforms
# ↳ generates OpenAPI clients
# ↳ starts Postgres + Redis + MinIO via docker-compose
# ↳ runs Flyway migrations

# 4. Pick your IDE and your workstream
open -a "Android Studio" android/    # ← Android dev opens THIS, not the repo root
open ios/Kehdo.xcworkspace            # ← iOS dev (or use `xed ios/`)
idea backend/                         # ← Backend dev opens the backend folder
code web/                             # ← Web dev (VS Code or any editor)
```

> **Important:** Open the **subfolder**, not the repo root, in your IDE. Each IDE only needs to index its own platform — opening the root will choke any IDE on the cross-platform mix.

---

## Day 1 — running each platform locally

### Backend (IntelliJ IDEA Ultimate or Community)

```bash
cd backend
docker-compose up -d                      # Postgres + Redis + MinIO
./gradlew :app:bootRun                    # http://localhost:8080
# → http://localhost:8080/swagger-ui.html
```

### Android (Android Studio Hedgehog 2023.1+)

1. Open `android/` directory (not the repo root)
2. Wait for Gradle sync
3. Run `:app` on a Pixel 6 emulator or physical device

### iOS (Xcode 15.2+)

1. Open `ios/Kehdo.xcworkspace` (the workspace, not the project)
2. Wait for Swift Packages to resolve
3. Run on iPhone 15 simulator (⌘R)

### Web (any editor + Node 20+)

```bash
cd web
pnpm install
pnpm dev                                  # http://localhost:3000
```

---

## How Claude works across IDEs (the magic)

When you open any folder in an editor with Claude (Claude Code, Cursor, Continue, etc.), Claude **automatically reads `CLAUDE.md` files** in the directory tree. We use this to keep four developers (or four IDE sessions) in perfect sync without anyone repeating themselves.

```
Open backend/ in IntelliJ
  → Claude reads /CLAUDE.md (universal rules)
  → Claude reads /backend/CLAUDE.md (Spring stack, AI pipeline rules)
  → Claude knows: Java 21, Gradle modules, no Lombok, Flyway-only migrations

Open android/ in Android Studio
  → Claude reads /CLAUDE.md (universal rules)
  → Claude reads /android/CLAUDE.md (Kotlin, Compose, MVI pattern)
  → Claude knows: Aurora colors come from :core:ui, never hardcode hex

Open ios/ in Xcode
  → Claude reads /CLAUDE.md (universal rules)
  → Claude reads /ios/CLAUDE.md (SwiftUI, Swift Packages, MVVM)
  → Claude knows: KH* package prefix, @Observable not Combine, iOS 17+
```

**Zero drift.** Every Claude session knows the brand, the tech stack, the git rules, the testing standards. New contributors get oriented the same way Claude does — by reading these files.

---

## The rules — short version

These are enforced by `/CLAUDE.md` and by branch protection. The full versions live in `/docs/GIT_WORKFLOW.md`.

1. **One repo, two long-lived branches** (`main` for prod, `develop` for integration).
2. **All work on feature branches** off `develop`. No exceptions.
3. **Claude never pushes** — ever. Claude commits locally, you push.
4. **No merges without testing + review.** Tests green. Manual QA confirmed. 1 reviewer minimum.
5. **API changes need 2 reviewers** — one from `@api-reviewers`, one from engineering.
6. **No merges on Fridays** — standard ops hygiene.
7. **Conventional Commits** — `feat(reply): add refine flow` etc.
8. **Hotfixes** branch off `main`, merge to BOTH `main` AND `develop`.

---

## What to build first — the 14-week roadmap

Already detailed in `/docs/master_plan.html`. Summary:

| Weeks | Focus | Output |
|-------|-------|--------|
| 1 | Repo bootstrap | Skeleton on GitHub, branch protection on, CI green |
| 2–3 | Backend auth + users | `api.staging.kehdo.app/auth/*` working |
| 3–4 | Android scaffold + auth screens | Signup → home empty shell on emulator |
| 5–8 | AI pipeline (OCR + speakers + LLM) | `POST /generate` returns 4 replies in <8s |
| 8–10 | Android full feature flow | Upload → reply → history → profile |
| 11–12 | Android beta + Play Store submission | App in closed beta, then production |
| 13–18 | iOS catches up to Android | TestFlight → App Store |
| 19+ | Phase 2: Share Intent extensions | High-value low-risk surfaces |

**Run the landing page in parallel from week 1** — it's lower risk than the backend and validates the brand publicly.

---

## Where to go next

| If you want to… | Read this |
|-----------------|-----------|
| Understand the architecture | `/docs/architecture.html` |
| Understand the git workflow | `/docs/GIT_WORKFLOW.md` |
| Make your first commit | `/CONTRIBUTING.md` |
| Work on backend | `/backend/CLAUDE.md` |
| Work on Android | `/android/CLAUDE.md` |
| Work on iOS | `/ios/CLAUDE.md` |
| Work on web | `/web/CLAUDE.md` |
| Edit the API spec | `/contracts/CLAUDE.md` |
| Edit design tokens | `/design/CLAUDE.md` |
| See decisions we've made | `/docs/adrs/` |

---

## Need help?

1. **Read the relevant `CLAUDE.md`** for your workstream first.
2. **Search `/docs/adrs/`** — we may have already documented the decision.
3. **Open a discussion** on GitHub — `Discussions` tab is enabled.
4. **Ask Claude** — every IDE session has full context once you open the right folder.

---

*Welcome to kehdo. Now go ship something.*
