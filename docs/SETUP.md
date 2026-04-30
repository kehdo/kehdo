# kehdo.app — Setup & Push Guide

> **Read this first** — it answers your two most important questions:
> 1. Should we push as one repo or many?
> 2. How does the CLAUDE.md system keep three IDEs in sync?

---

## ✅ The answer to your push strategy question

**Use ONE GitHub repository named `kehdo`.** Not separate repos per platform.

### Why one repo (monorepo)

| Reason | Detail |
|--------|--------|
| **Atomic commits** | One PR can change backend + Android + iOS for a single feature |
| **Single source of truth** | `/contracts/` and `/design/` define the API and brand once |
| **Easier releases** | One git tag = one coordinated release across all platforms |
| **Smaller cognitive load** | One README, one CLAUDE.md hierarchy, one branch protection setup |
| **What big companies do** | Google, Shopify, Airbnb, Stripe, Linear all use monorepos |

### Why NOT separate repos per platform

If you put backend, Android, and iOS in three different GitHub repos:

- A small API change requires 3 PRs in 3 repos, coordinated manually
- Generated API clients can drift between platforms
- Design tokens have to be published as a separate package, versioned independently
- New developers need to clone 3+ repos to make sense of the project
- CI can't run integration tests across platforms easily

The downsides of one big repo (clone size, IDE-must-scope-to-subdir) are easily mitigated. The downsides of multi-repo are structural and grow with time.

---

## 🚀 First-time push to GitHub — exact commands

This is the **only push your run for the initial setup**. After this, every change goes through feature branches with PR + review.

### Step 1 — Create the empty GitHub repo

Go to [github.com/new](https://github.com/new):
- **Owner:** your org (or your username)
- **Repository name:** `kehdo` (lowercase, exact)
- **Description:** "kehdo.app — reply with quiet confidence."
- **Visibility:** Private (until launch)
- **Initialize:** ❌ Do NOT add README, .gitignore, or license. The skeleton already has these.

Click **Create repository**.

### Step 2 — Initialize git locally

```bash
# Extract the skeleton ZIP somewhere, e.g.:
cd ~/projects
unzip kehdo-skeleton.zip
cd kehdo

# Initialize git
git init
git branch -M main

# Stage everything
git add .

# First commit
git commit -m "chore: initial repo scaffolding with CLAUDE.md system

- Monorepo structure: backend, android, ios, web, contracts, design, extensions
- CLAUDE.md hierarchy for AI-assisted development across IDEs
- OpenAPI 3.1 contract with auth, conversation, reply, tone endpoints
- Aurora palette tokens (colors, typography, spacing) in JSON
- 5 ADRs documenting key architectural decisions
- CI workflows for backend, android, ios, web, contracts
- Bootstrap script and design token / OpenAPI client generators"
```

### Step 3 — Push main and create develop

```bash
# Add the GitHub remote (replace <YOUR-ORG>)
git remote add origin git@github.com:<YOUR-ORG>/kehdo.git

# Push main
git push -u origin main

# Create and push develop (the integration branch)
git checkout -b develop
git push -u origin develop

# Set develop as your default working branch
git branch --set-upstream-to=origin/develop develop
```

### Step 4 — Configure branch protection on GitHub

Go to **Settings → Branches → Add branch protection rule**.

#### For `main`:
- ✅ Require a pull request before merging
  - ✅ Require approvals: **1**
  - ✅ Dismiss stale reviews when new commits are pushed
  - ✅ Require review from Code Owners
- ✅ Require status checks to pass before merging
  - Required: backend CI, android CI, ios CI, web CI, contracts CI
- ✅ Require conversation resolution before merging
- ✅ Require linear history
- ❌ Do not allow force pushes
- ❌ Do not allow deletions

#### For `develop`:
- ✅ Require a pull request before merging
  - ✅ Require approvals: **1**
- ✅ Require status checks to pass before merging
- ❌ Do not allow force pushes

### Step 5 — Set up CODEOWNERS teams (optional, recommended)

In your GitHub org, create teams referenced in `.github/CODEOWNERS`:
- `@<org>/engineering` — everyone
- `@<org>/api-reviewers` — for contract changes
- `@<org>/design-reviewers` — for token changes
- `@<org>/backend-team`, `@<org>/android-team`, `@<org>/ios-team`, `@<org>/web-team`
- `@<org>/platform-team` — for infra/CI changes
- `@<org>/tech-leads` — for ADRs and CLAUDE.md changes

If you're a solo developer, just remove the team references from `.github/CODEOWNERS` for now.

---

## 🌿 Day-to-day workflow — every feature

After the initial push, you'll never push to `main` or `develop` directly. Every change goes:

```
feat/* branch  →  PR  →  develop  →  PR  →  main (release)
```

### Example: building the landing page

```bash
# Always start from develop
git checkout develop
git pull origin develop

# Create a feature branch
git checkout -b feat/web/landing-hero

# Open VS Code in the web folder
code web/

# Claude reads /CLAUDE.md + /web/CLAUDE.md automatically
# Claude writes the Hero component, runs tests, commits
# Claude tells you to push:

git push origin feat/web/landing-hero

# Open a PR on GitHub from feat/web/landing-hero → develop
# Wait for CI green, get a review, merge to develop

# Vercel automatically deploys develop to staging.kehdo.app
# When ready for production, open a PR from develop → main
# After merge, tag the release: git tag v1.0.0
```

---

## 🧠 The CLAUDE.md system — how three IDEs stay in sync

### How it works

Claude (and similar AI tools) **automatically** read `CLAUDE.md` files in the directory they're operating in. By placing one at the root and one in each workstream, every Claude session gets the right context.

### The hierarchy

```
kehdo/
├── CLAUDE.md                ← UNIVERSAL — read by every session
├── backend/CLAUDE.md        ← Spring Boot context
├── android/CLAUDE.md        ← Kotlin + Compose context
├── ios/CLAUDE.md            ← Swift + SwiftUI context
├── web/CLAUDE.md            ← Next.js context
├── contracts/CLAUDE.md      ← OpenAPI rules
├── design/CLAUDE.md         ← Token rules
└── extensions/CLAUDE.md     ← Future surfaces (placeholder)
```

### What each session sees

| You open this... | In this IDE | Claude loads |
|------------------|-------------|--------------|
| `backend/` | IntelliJ IDEA | `/CLAUDE.md` + `/backend/CLAUDE.md` |
| `android/` | Android Studio | `/CLAUDE.md` + `/android/CLAUDE.md` |
| `ios/Kehdo.xcworkspace` | Xcode | `/CLAUDE.md` + `/ios/CLAUDE.md` |
| `web/` | VS Code | `/CLAUDE.md` + `/web/CLAUDE.md` |

### Why this works

- **Zero drift** between platforms — they all read the same root CLAUDE.md
- **Platform-specific rules** stay scoped to that platform's CLAUDE.md
- **No context loss** — every Claude session knows the brand, git rules, conventions
- **Onboarding is automatic** — new devs get oriented the same way Claude does

---

## 🛠️ Setting up your dev machine

### Required tools

```bash
# macOS (required for iOS work)
brew install openjdk@21 node@20 gradle docker openapi-generator gh
brew install --cask android-studio intellij-idea-ce visual-studio-code

# For iOS (macOS only)
xcode-select --install
brew install swiftlint swiftformat

# pnpm for the web project
npm install -g pnpm@9

# Fastlane for iOS releases
gem install fastlane
```

### Pick your IDE per workstream

Each developer typically opens **only the folder they're working in**, not the repo root. This keeps IDE indexing fast and prevents cross-contamination.

```bash
# Backend developers
idea backend/

# Android developers
open -a "Android Studio" android/

# iOS developers
open ios/Kehdo.xcworkspace

# Web developers
code web/
cd web && pnpm install && pnpm dev
```

If you work across platforms (rare — usually only tech leads), you can open the repo root in VS Code and use the Multi-root workspace feature to navigate.

---

## 🎯 Implementation order — what to build next

After the initial push and branch protection setup, here's the recommended build order:

### Phase 0 — Repo alive (Week 1)

- [x] Skeleton committed and pushed
- [x] Branch protection configured
- [ ] CI green on a no-op PR (test the workflow)
- [ ] Vercel project connected for `web/`
- [ ] AWS account set up for backend deployment (Phase 3)

### Phase 1 — Landing page (Days 2–4)

Branch: `feat/web/landing-page`
- Port the Aurora mockup to the Next.js app router
- Wire up the waitlist email capture (Google Sheets via Apps Script)
- Add Vercel Web Analytics
- Deploy to Vercel — `kehdo.app` live

### Phase 2 — Backend auth (Weeks 2–3)

Branches: `feat/be/scaffold`, `feat/be/auth`
- Implement signup, login, refresh, logout endpoints
- Flyway migrations for users + sessions
- Deploy to staging at `api.staging.kehdo.app`

### Phase 3 — Android auth (Weeks 3–4, parallel)

Branches: `feat/and/scaffold`, `feat/and/onboarding`, `feat/and/auth`
- All 15 Gradle modules wired up
- Onboarding + Auth + empty Home screen
- Generated Retrofit client from OpenAPI

### Phase 4 — AI pipeline (Weeks 5–8)

The crown jewels. OCR, speaker attribution, LLM orchestration.

### Phase 5 — Android full flow (Weeks 8–10)

Upload, reply, history, profile, paywall.

### Phase 6 — Android beta + launch (Weeks 11–12)

Closed beta, Play Store submission.

### Phase 7 — iOS (Weeks 13–18, parallel start at week 11)

Same scope, mirrors Android.

### Phase 8+ — Extensions (Q3+)

Android Share Intent → iOS Share Sheet → Chrome → Keyboards.

---

## 🚦 Rules summary

### Claude will

- ✅ Write code in feature branches
- ✅ Run `git status`, `git diff`, `git add`, `git commit` locally
- ✅ Run tests and report results
- ✅ Generate Conventional Commits messages
- ✅ Give you exact `git push` commands to run yourself

### Claude will never

- ❌ Push to any remote
- ❌ Touch `main` or `develop` directly
- ❌ Merge a PR
- ❌ Add a dependency without asking
- ❌ Modify contracts or design tokens without asking
- ❌ Bypass the feature branch flow

### You will

- ✅ Run the push commands Claude gives you
- ✅ Open PRs on GitHub
- ✅ Review the PR (you or a teammate)
- ✅ Confirm the feature works (manual QA)
- ✅ Merge after CI passes + review approves
- ✅ Tag releases when ready for production

---

## 📞 If something goes wrong

| Issue | Fix |
|-------|-----|
| CI fails on a fresh PR | Check the workflow run logs in the Actions tab |
| Branch protection blocks you | You're trying to push to `main` or `develop` directly — use a feature branch |
| `bootstrap.sh` fails | Check that all required tools are installed (`brew bundle` from `Brewfile` if you have one) |
| Generated code doesn't appear | Run `./tools/generate-tokens.sh` and `./tools/generate-clients.sh` manually |
| Claude in one IDE makes a change that breaks another | Both Claudes should have read the root `/CLAUDE.md` — file an issue if rules conflict |

---

## 🎨 Brand reminder

- **Name:** kehdo (कह दो — "say it" in Hindi)
- **Domain:** kehdo.app
- **Never use** the old name "Flawlessly" anywhere — it's deprecated
- **Aurora palette** is locked in `/design/tokens/colors.json`

---

## 📋 Operator runbook — waitlist via Google Apps Script

The landing page's `/api/waitlist` route forwards POSTed emails to a Google Apps Script web app that appends them to a Google Sheet. This avoids a paid SaaS dependency for pre-launch lead capture. Setup steps:

### 1. Create the sheet

1. https://sheets.google.com → **Blank** spreadsheet
2. Rename: **kehdo waitlist**
3. Row 1 headers (cells A1, B1, C1): `Email`, `Source`, `Timestamp`
4. View → Freeze → 1 row (so headers stay visible)

### 2. Paste the Apps Script handler

**Extensions → Apps Script** → replace the default code with:

```javascript
function doPost(e) {
  try {
    const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    const data = JSON.parse(e.postData.contents);

    if (!data.email) {
      return jsonResponse({ error: "email_required" });
    }

    // Dedupe: scan column A (skip header row) for existing email
    const lastRow = sheet.getLastRow();
    const existing = lastRow > 1
      ? sheet.getRange("A2:A" + lastRow).getValues().flat().filter(Boolean)
      : [];

    if (existing.includes(data.email)) {
      return jsonResponse({ ok: true, alreadyOnList: true });
    }

    sheet.appendRow([
      data.email,
      data.source || "landing",
      data.timestamp || new Date().toISOString(),
    ]);

    return jsonResponse({ ok: true });
  } catch (err) {
    console.error(err);
    return jsonResponse({ error: "server_error" });
  }
}

function jsonResponse(payload) {
  return ContentService
    .createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}
```

Save (Ctrl+S). Rename the project to `kehdo-waitlist-handler`.

### 3. Deploy as web app

1. **Deploy → New deployment** → gear icon → **Web app**
2. Settings:
   - Description: `kehdo waitlist v1`
   - Execute as: **Me** (your Google account)
   - Who has access: **Anyone** ⚠️ (required so the Vercel function can call it without auth)
3. **Deploy** → authorize the OAuth scopes (Spreadsheets) on first run
4. Copy the **Web app URL** — looks like `https://script.google.com/macros/s/AKfyc.../exec`

### 4. Wire into Vercel

Vercel → kehdo project → **Settings → Environment Variables** → add:

- `GOOGLE_SHEET_WEBHOOK_URL` = the deployment URL
- Scope: Production + Preview + Development

Save, then redeploy (env-var changes don't apply to existing deployments).

### 5. Smoke test

Submit your own email through the form on `staging.kehdo.app` → check the sheet within ~3s for the new row. Submit again → form succeeds, sheet doesn't gain a duplicate (idempotent dedupe).

### Updating the script later

If you edit the Apps Script, **Deploy → Manage deployments → ⋯ → Edit → New version → Deploy** to update the same URL. The default "Save" only persists in the editor — it doesn't redeploy.

### Migrating off Apps Script

When you outgrow the sheet (ballpark: a few thousand signups, or when you need transactional email), export the sheet (File → Download → CSV) and import into your email service of choice (Mailchimp, ConvertKit, Buttondown, etc.). The `/api/waitlist` route can be re-pointed without UI changes.

---

*Master setup guide v1.0 — extend this doc when conventions change.*
