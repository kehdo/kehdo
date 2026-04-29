# CLAUDE.md — Design Context

> Loaded **in addition to** `/CLAUDE.md`. Single source of truth for visual design tokens.

---

## 🎯 Purpose

Colors, typography, spacing, motion, and microcopy live here in **JSON**. They generate native code for each platform. **This is the only place where these values are defined.**

---

## 📂 Structure

```
design/
├── CLAUDE.md
├── tokens/
│   ├── colors.json             ← Aurora palette
│   ├── typography.json         ← Inter + Instrument Serif scale
│   ├── spacing.json            ← 4pt grid
│   ├── radii.json              ← border-radius
│   └── motion.json             ← easing, durations
├── copy/
│   ├── en.json                 ← English (primary)
│   ├── hi.json                 ← Hindi
│   ├── es.json                 ← Spanish
│   └── pt.json                 ← Portuguese
├── assets/                     ← logo SVGs, Lottie (Git LFS)
│   ├── logo/
│   ├── icons/
│   └── lottie/
└── mockups/                    ← reference HTML mockups
```

---

## 🎨 Aurora palette — LOCKED

These values are final. Any change here triggers regeneration in all platforms.

```json
{
  "bg": "#0A0612",
  "bg2": "#120A1F",
  "surface": "#1A0F2E",
  "surface2": "#24173D",
  "purple": "#9C5BFF",
  "purpleBright": "#B47BFF",
  "purpleDeep": "#6B2FD9",
  "pink": "#EC4899",
  "amber": "#F59E0B",
  "blue": "#3B82F6",
  "text": "#F5F3FF",
  "textDim": "rgba(245,243,255,0.65)",
  "textMute": "rgba(245,243,255,0.45)",
  "line": "rgba(255,255,255,0.08)",
  "success": "#10B981",
  "gradAurora": {
    "stops": [
      { "color": "#9C5BFF", "position": 0 },
      { "color": "#EC4899", "position": 50 },
      { "color": "#F59E0B", "position": 100 }
    ],
    "angle": 135
  }
}
```

---

## 🔤 Typography

**Primary:** Inter (400–900)
**Accent:** Instrument Serif Italic (gradient headlines only)

Scale: `display1` 96px → `caption` 12px (see `typography.json`).

---

## 📐 Spacing scale (4pt grid)

```
0: 0   |  1: 4   |  2: 8   |  3: 12   |  4: 16
5: 24  |  6: 32  |  7: 48  |  8: 64   |  9: 96
```

---

## 🌐 Microcopy rules

All user-facing strings live in `copy/<lang>.json`. Built into platform-native i18n (Android `strings.xml`, iOS `Localizable.xcstrings`, Web JSON imports).

**Launch languages:** English, Hindi, Spanish, Portuguese.

```json
{
  "onboarding": {
    "welcome.title": "Reply with quiet confidence.",
    "welcome.subtitle": "Drop a chat screenshot. Get the perfect reply in seconds.",
    "welcome.cta": "Get started"
  },
  "errors": {
    "RATE_LIMIT_EXCEEDED": "You've hit your daily limit of {{limit}} replies. Upgrade for unlimited."
  }
}
```

**Rules:**
- Keys dot-namespaced: `<screen>.<element>.<variant>`
- Placeholders: `{{name}}` syntax
- Error keys match codes in `contracts/errors/codes.yaml`
- Every English string must have `hi`, `es`, `pt` translations before merge

---

## 🔄 Generation

`./tools/generate-tokens.sh` runs:

| Input | Output | Consumer |
|-------|--------|----------|
| `colors.json` | `android/core/core-ui/.../Color.kt` | Android |
| `colors.json` | `ios/Packages/KHDesignSystem/.../Colors.swift` | iOS |
| `colors.json` | `web/src/styles/tokens.generated.css` | Web |
| `copy/en.json` | `android/.../res/values/strings.xml` | Android |
| `copy/en.json` | `ios/.../Localizable.xcstrings` | iOS |

**Generated files are gitignored.** Always edit JSON source, never the output.

---

## 🚫 Do NOT

- Add a new color without justification — the palette is small on purpose
- Use a color in code that isn't from a token
- Add English copy without all 4 translations
- Edit generated output files
- Commit binary assets (logos, Lottie) without Git LFS

---

## ✅ Adding a new color

1. Justify the use case (Claude will ask)
2. Add to `colors.json` with a semantic name
3. Run `./tools/generate-tokens.sh`
4. Use via the generated constant
5. Commit: `feat(design): add warning color for upgrade banners`

---

## ✅ Adding a new string

1. Add to `copy/en.json`
2. Translate to `hi.json`, `es.json`, `pt.json`
3. Run `./tools/generate-tokens.sh`
4. Use via Android `R.string.<key>`, iOS `String(localized: "<key>")`, Web `t("<key>")`
5. Never concatenate translated strings — use placeholders

---

*Design context v1.0*
