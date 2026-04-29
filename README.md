# kehdo.app

> **Reply with quiet confidence.**
> AI-powered reply generator for chat screenshots. Drop a screenshot, get the perfect reply in seconds.

---

## ⚡ Quick links

- **Start here:** [`docs/master_plan.html`](docs/master_plan.html) — the implementation plan
- **Architecture:** [`docs/architecture.html`](docs/architecture.html)
- **Brand & logo:** [`docs/logo_brief.html`](docs/logo_brief.html)
- **AI rules:** [`CLAUDE.md`](CLAUDE.md) — universal context for Claude

---

## 📁 Repository layout

This is a **monorepo**. All platforms live in this single git repository.

```
kehdo/
├── CLAUDE.md              ← universal context (read first, always)
├── README.md              ← you are here
├── contracts/             ← OpenAPI spec — single source of truth for the API
├── design/                ← Aurora palette, tokens, microcopy in JSON
├── backend/               ← Spring Boot 3.2 + Java 21 + PostgreSQL 16
├── android/               ← Kotlin 1.9 + Jetpack Compose
├── ios/                   ← Swift 5.9 + SwiftUI (iOS 17+)
├── web/                   ← Next.js 14 — landing page at kehdo.app
├── extensions/            ← RESERVED — Phase 2 (Chrome, Share, Keyboard)
├── infra/                 ← Terraform + Kubernetes + docker-compose
├── tools/                 ← bootstrap, generators, helpers
├── docs/                  ← architecture docs, ADRs, master plan
└── .github/               ← CI workflows, PR template, CODEOWNERS
```

Each workstream folder has its own `CLAUDE.md` with platform-specific rules.

---

## 🚀 First-time setup

### Prerequisites

```bash
# macOS (required for iOS work)
brew install openjdk@21 node@20 gradle xcodegen swiftlint swiftformat
brew install --cask android-studio intellij-idea-ce docker
xcode-select --install

# Cross-platform tooling
brew install openapi-generator gh
npm install -g pnpm
gem install fastlane
```

### Clone & bootstrap

```bash
git clone git@github.com:<your-org>/kehdo.git
cd kehdo
./tools/bootstrap.sh
```

The bootstrap script:
1. Generates design tokens for all platforms
2. Generates API clients from the OpenAPI spec
3. Starts local Postgres + Redis + MinIO via Docker
4. Runs database migrations
5. Seeds a test user

### Open in your IDE

```bash
# Android developers
open -a "Android Studio" android/

# iOS developers
open ios/Kehdo.xcworkspace

# Backend developers
idea backend/

# Web developers
code web/
```

---

## 🌿 Branching & contributions

We use **Git Flow**:

```
main      ← production (tagged releases only)
 ↑
develop   ← integration
 ↑
feat/*    ← all feature work happens here
```

Branch naming: `feat/<scope>/<description>`, where scope is `be`, `and`, `ios`, `web`, `api`, `design`, or `infra`.

**Never push directly to `main` or `develop`.** All changes go through pull requests with CI + manual review + QA confirmation.

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the full workflow.

---

## 🧠 AI-assisted development

This repo is optimized for Claude (and any AI tool that reads `CLAUDE.md`).

When you open any folder in an AI-enabled IDE:
- **Root `/CLAUDE.md`** is loaded automatically (universal rules)
- **Folder-specific `CLAUDE.md`** is loaded automatically (platform rules)

This means Claude in Android Studio knows the Android conventions; Claude in Xcode knows the iOS conventions; Claude in IntelliJ knows the backend conventions. Zero drift between platforms.

---

## 🎨 Brand

- **Name:** kehdo (कह दो — "say it" in Hindi)
- **Domain:** kehdo.app
- **Palette:** Aurora — purple → pink → amber gradient on a deep violet canvas
- **Typography:** Inter (primary) + Instrument Serif Italic (gradient accents)
- **Full spec:** [`docs/logo_brief.html`](docs/logo_brief.html)

---

## 📞 Environments

| Environment | URL | Branch | Auto-deploy |
|-------------|-----|--------|-------------|
| Production  | `kehdo.app`, `api.kehdo.app` | `main` | Manual approval |
| Staging     | `staging.kehdo.app`, `api.staging.kehdo.app` | `develop` | Auto on merge |
| Local       | `localhost:8080` (API), `localhost:3000` (web) | any | Manual |

---

## 📚 Key documentation

| Doc | Purpose |
|-----|---------|
| [`CLAUDE.md`](CLAUDE.md) | Universal AI assistant rules |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | How to contribute, branch flow, commit format |
| [`SECURITY.md`](SECURITY.md) | Security policy, vulnerability reporting |
| [`docs/master_plan.html`](docs/master_plan.html) | Step-by-step implementation plan |
| [`docs/architecture.html`](docs/architecture.html) | Technical architecture (16 sections) |
| [`docs/implementation.html`](docs/implementation.html) | Repo structure deep dive |
| [`docs/logo_brief.html`](docs/logo_brief.html) | Aurora palette + logo brief |
| [`docs/adrs/`](docs/adrs/) | Architecture Decision Records |

---

## 📜 License

Proprietary © 2026 kehdo.app — all rights reserved.

---

*Built with care, deployed with rigor.*
