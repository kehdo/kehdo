# CLAUDE.md — Web (Landing Page) Workstream

> Loaded **in addition to** the root `/CLAUDE.md`.
> Read the root file first for universal rules.

---

## 🎯 Scope

This directory holds the kehdo marketing landing page deployed at **kehdo.app**. Open `web/` in any editor (VS Code, Cursor, etc.) — Node 20+ required.

**This is NOT the app.** The actual product lives in `android/` and `ios/`. The landing page exists to:
1. Capture beta waitlist signups
2. SEO discovery
3. App Store / Play Store deep-link routing
4. Privacy policy + ToS hosting

---

## 🧱 Tech stack — locked

| Layer | Choice | Reason |
|-------|--------|--------|
| Framework | Next.js 14 (App Router) | SSR for SEO, easy Vercel deploy |
| Language | TypeScript 5.3+ (strict) | Type safety |
| Styling | Tailwind CSS 3.4 | Matches our HTML mockups |
| Forms | react-hook-form + Zod | Validation + minimal re-renders |
| Animation | Framer Motion | Aurora orbs, scroll-triggered |
| Icons | lucide-react | Clean, tree-shaken |
| Email | Resend | Waitlist signups |
| Analytics | Plausible | Privacy-friendly, on-brand |
| Testing | Vitest + Testing Library | Fast unit tests |
| E2E | Playwright | One smoke test for the landing flow |
| Hosting | Vercel | Best-in-class Next.js host |
| Package manager | pnpm 8+ | Fast, disk-efficient |

---

## 📂 Structure

```
web/
├── package.json                 ← deps + scripts
├── next.config.mjs              ← Next.js config
├── tailwind.config.ts           ← Aurora palette wired in
├── tsconfig.json                ← strict mode
├── postcss.config.mjs
├── .env.example
├── public/                      ← static assets, OG images, favicon
└── src/
    ├── app/                     ← App Router pages
    │   ├── layout.tsx           ← root layout, fonts, metadata
    │   ├── page.tsx             ← landing page
    │   ├── globals.css          ← Tailwind + Aurora vars
    │   └── api/
    │       └── waitlist/route.ts ← POST email → Resend
    ├── components/              ← reusable React components
    │   ├── Hero.tsx
    │   ├── HowItWorks.tsx
    │   ├── Bento.tsx
    │   ├── Pricing.tsx
    │   ├── Faq.tsx
    │   ├── Footer.tsx
    │   └── WaitlistForm.tsx
    ├── lib/                     ← helpers
    │   └── cn.ts                ← Tailwind class merger
    ├── styles/
    │   └── globals.css
    └── generated/               ← (gitignored) OpenAPI TypeScript types
```

---

## 🎨 Design system

**The Aurora palette is wired into Tailwind** in `tailwind.config.ts`:

```ts
colors: {
  bg: "#0A0612",
  surface: "#1A0F2E",
  purple: { DEFAULT: "#9C5BFF", bright: "#B47BFF", deep: "#6B2FD9" },
  pink: "#EC4899",
  amber: "#F59E0B",
  blue: "#3B82F6",
  moonlight: "#F5F3FF",
}
```

```tsx
// ✅ Use Tailwind classes from the palette
<button className="bg-aurora-gradient text-white">
<div className="bg-surface text-moonlight">

// ❌ Don't inline hex values
<div style={{ background: "#9C5BFF" }}>
```

Fonts (Inter + Instrument Serif) load via `next/font/google` in `app/layout.tsx`.

---

## 📐 Required landing page sections — locked

The page must include all of these, in this order. They're already drafted in `/docs/landing_mockup.html` — port that HTML into React components.

1. **Top nav** — sticky, transparent → solid on scroll
2. **Hero** — gradient headline, two CTAs, four stat tiles
3. **Product preview** — Linear-style chrome showing live reply generation
4. **How it works** — 3 numbered steps (upload → parse → pick reply)
5. **Features bento grid** — 18 tones, <6s, 97% accuracy, privacy, 30+ languages
6. **Privacy section** — 30-day auto-delete, zero training, on-device OCR option, SOC 2
7. **Free tier callout** — "5 replies/day forever, no ads, no dark patterns"
8. **Pricing table** — Starter $0, Pro $4/mo yearly, Unlimited $12/mo
9. **Testimonials** — 3 quotes (drafted in copy/en.json)
10. **Comparison table** — vs. typing yourself, vs. generic AI
11. **Trust signals** — "Powered by Claude Opus 4.7"
12. **FAQ** — 6 questions (privacy, languages, cancel, etc.)
13. **Final CTA + waitlist form** — "Your next reply is about to be flawless."
14. **Footer** — links, app store buttons, social

**Do NOT add or remove sections without asking the user first.**

---

## 🌐 i18n — Phase 1.5

Launch in English. Add Hindi, Spanish, Portuguese after first traffic data.

- Use `next-intl` for App Router i18n
- Pull strings from `/design/copy/<lang>.json` (same source as the apps)
- URLs: `kehdo.app/` (en), `/hi`, `/es`, `/pt`
- Hreflang tags for SEO

---

## 🏃 Running locally

```bash
cd web
pnpm install                              # use pnpm, not npm
cp .env.example .env.local                # add Resend key
pnpm dev                                  # http://localhost:3000

# Other scripts
pnpm typecheck                            # tsc --noEmit
pnpm lint                                 # next lint
pnpm test                                 # vitest run
pnpm test:e2e                             # playwright (requires browsers installed)
pnpm build                                # production build
pnpm format                               # prettier --write
```

---

## 🚀 Deployment

- **Hosting:** Vercel
- **Custom domain:** `kehdo.app` (production), `staging.kehdo.app` (auto-deploys from `develop`)
- **Branch deploys:** auto-created for every PR — useful for design review
- **Production deploy:** only from `main` after manual approval in Vercel
- **Env vars:** set in Vercel project settings (not committed)

---

## 🚫 Do NOT

- ❌ Hardcode hex values — use Tailwind classes from the Aurora palette
- ❌ Hardcode user-facing strings — use `/design/copy/en.json`
- ❌ Use `useState` for form values — use `react-hook-form`
- ❌ Use `<img>` — always `<Image>` from `next/image` for optimization
- ❌ Use `<a href>` for internal nav — always `<Link>` from `next/link`
- ❌ Use Google Analytics — use Plausible (privacy-friendly is a brand value)
- ❌ Add a new section to the landing page without asking
- ❌ Use any state management library (Redux, Zustand) — landing page should be mostly stateless
- ❌ Add server-side state — Vercel functions are stateless; talk to backend API instead

---

## ✅ When making changes

1. **Branch:** `feat/web/<description>` (e.g., `feat/web/add-pricing-section`)
2. **Edit components** in `src/components/`
3. **Verify visually** at `localhost:3000` AND on a Vercel preview deploy
4. **Lighthouse score must stay 90+** (Performance, Accessibility, Best Practices, SEO)
5. **Tests:** smoke test for waitlist form; visual snapshot for major sections
6. **Ask Claude for commit + push commands.** Claude does NOT push.

---

## 🎯 SEO requirements

Every page must have:
- `<title>` (50–60 chars)
- `<meta name="description">` (150–160 chars)
- OpenGraph tags (`og:title`, `og:description`, `og:image`)
- Twitter Card tags
- JSON-LD `Product` schema on the home page
- `sitemap.xml` and `robots.txt` (Next.js generates these)

OG images are 1200×630 PNG. Generate with the Aurora gradient + kehdo wordmark — the design is in `/docs/logo_brief.html`.

---

*Web context version: 1.0*
