# CLAUDE.md — Extensions Context

> Loaded **in addition to** `/CLAUDE.md`. This directory is **reserved for future surfaces.**

---

## 🎯 Purpose

All non-primary surfaces that will eventually exist:
- Chrome/Edge browser extension
- Android Share Intent target
- iOS Share Sheet extension
- Keyboard extensions (Android IME, iOS Keyboard Extension)

**These don't exist yet.** This folder exists to:
1. Claim the space so nobody stuffs extension code in main apps
2. Document rules for when they're built
3. Be a reminder of Phase 2 / Phase 3 roadmap

---

## 📂 Planned structure

```
extensions/
├── CLAUDE.md
├── chrome/                     (Phase 2)
├── android-share/              (Phase 2)
├── ios-share/                  (Phase 2)
├── keyboard-android/           (Phase 3)
└── keyboard-ios/               (Phase 3)
```

---

## 🔒 THE GOLDEN RULE

**Everything here is a THIN SHELL around the core apps.**

- Chrome extension: 90% UI + 10% API call
- Android Share Intent: 100% launching `:feature:reply` with shared image URI
- iOS Share Sheet: ShareViewController importing `KHDomain` + `KHDesignSystem`
- Keyboard extensions: UI chrome around an existing generate flow

If a new extension needs business logic, **that logic belongs in `domain/`** (or `KHDomain` on iOS) — never here. If this folder grows domain models, use-cases, or API clients, the architecture has failed.

---

## 🛠️ Day-1 preparations (already done)

To make Phase 2 trivial:

### Android
- `:feature:reply` accepts `Uri` from any source — Share Intent passes `content://` URI, screen works unchanged
- Auth tokens stored in AccountManager — extensions read without re-auth
- Intent filters added to AndroidManifest in Phase 2

### iOS
- `KHReply` uses a `ReplyEntryPoint` protocol — accepts any image URL source
- Keychain Sharing entitlement with access group `$(AppIdentifierPrefix)app.kehdo.shared`
- App Group: `group.app.kehdo.shared`

### Backend
- OpenAPI spec includes `X-Client-Surface` header enum (`ANDROID_APP`, `IOS_APP`, `CHROME_EXT`, `ANDROID_SHARE`, etc.)
- Auth endpoints accept short-lived scoped tokens for sandboxed extensions

---

## 📋 Phase priorities

When ready, build in this order (highest user impact first):

1. **Android Share Intent** — highest value, lowest risk
2. **iOS Share Sheet** — mirror, reuses KHDomain
3. **Chrome extension** — clipboard paste support
4. **Android Keyboard (IME)** — trust-heavy, later
5. **iOS Keyboard Extension** — most sandboxed, last

---

## 🚫 Do NOT (until populated)

- Don't create files here yet
- Don't add Gradle modules or Swift Packages prematurely
- Don't put "future extension code" in main apps "just for now"

---

*Extensions context v1.0 (placeholder)*
