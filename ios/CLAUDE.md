# CLAUDE.md — iOS Workstream

> Loaded **in addition to** the root `/CLAUDE.md`.
> Read the root file first for universal rules.

---

## 🎯 Scope

This directory holds the kehdo iOS app. **Open `Kehdo.xcworkspace`** (not the .xcodeproj) in Xcode 15.2+.

- **Bundle ID:** `app.kehdo.ios`
- **Min iOS:** 17.0 (needed for SwiftData, `@Observable`, modern SwiftUI)
- **Swift:** 5.9+
- **Xcode:** 15.2+

---

## 🧱 Tech stack — locked

| Layer | Choice | Reason |
|-------|--------|--------|
| Language | Swift 5.9+ | Modern, safe, fast |
| UI | SwiftUI (iOS 17+) | Aligned with Android Compose mental model |
| State | `@Observable` + MVVM | Modern, no Combine boilerplate |
| Network | URLSession + async/await | No third-party HTTP lib |
| Persistence | SwiftData | Modern Core Data replacement |
| Secure storage | Keychain (with sharing) | Apple-recommended for tokens |
| Auth | Firebase Auth | Matches Android |
| Analytics | Mixpanel iOS SDK | Matches Android |
| Crash reports | Sentry iOS | Matches Android |
| Testing | XCTest + swift-snapshot-testing | Standard + snapshot tests |
| Linting | SwiftLint + SwiftFormat (Homebrew) | Style consistency |
| Build | Xcode + Swift Package Manager | Native, no CocoaPods |
| Release | Fastlane | TestFlight + App Store automation |

---

## 📂 Module structure — local Swift Packages

```
ios/
├── Kehdo.xcworkspace/             ★ open this, not the .xcodeproj
├── Kehdo.xcodeproj/
├── App/                           main app target
│   ├── KehdoApp.swift              @main entry
│   ├── Navigation/AppCoordinator.swift
│   └── Resources/
├── Packages/                       local Swift Packages = modules
│   ├── KHCore/                    Outcome, KehdoError, Logger
│   ├── KHDesignSystem/            AuroraColors, KehdoTheme, components
│   ├── KHNetwork/                 URLSession + OpenAPI-generated client
│   ├── KHPersistence/             SwiftData + Keychain
│   ├── KHDomain/                  pure Swift, use-cases (mirrors Android domain)
│   ├── KHData/                    repository implementations
│   └── Features/
│       ├── KHOnboarding/
│       ├── KHAuth/
│       ├── KHHome/
│       ├── KHUpload/
│       ├── KHReply/
│       ├── KHHistory/
│       ├── KHProfile/
│       └── KHPaywall/
├── Tests/
│   ├── UnitTests/
│   ├── SnapshotTests/
│   └── UITests/
├── Scripts/
│   ├── generate-tokens.sh
│   └── generate-openapi.sh
└── fastlane/Fastfile
```

All packages prefixed with `KH` (for Kehdo). Avoids name clashes with Apple/third-party.

---

## 🔒 Dependency rules — enforced by Package.swift

A Swift Package can only `import` modules declared in its `Package.swift` dependencies.

| Package | May depend on | May NOT depend on |
|---------|---------------|-------------------|
| `App` | All `KH*` features, all Core packages | Direct `KHData` imports (use protocols) |
| `KHFeatures/*` | `KHDomain`, `KHDesignSystem`, `KHCore` | Another feature, `KHData`, `KHNetwork` |
| `KHData` | `KHDomain`, `KHNetwork`, `KHPersistence` | Any feature |
| `KHDomain` | `KHCore` only | Anything UIKit/SwiftUI |
| `KHCore*` | Rarely other Core | Features, `KHData` |

---

## 🎨 Design system usage

All tokens generated from `/design/tokens/*.json` into `KHDesignSystem/Sources/Tokens/`. Never hardcode.

```swift
// ❌ WRONG
Text("Hello").foregroundColor(Color(red: 0.61, green: 0.36, blue: 1.0))

// ✅ RIGHT
Text("Hello").foregroundColor(AuroraColors.purple)
```

```swift
// Apply theme at the app root
@main
struct KehdoApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
                .kehdoTheme()
        }
    }
}
```

---

## 🧠 State management — MVVM + @Observable (locked)

Every screen follows this exact shape. Aligns mentally with Android's MVI.

```swift
// 1. State — Equatable struct
public struct SignInState: Equatable {
    public var email: String = ""
    public var password: String = ""
    public var isLoading: Bool = false
    public var errorCode: String? = nil
}

// 2. ViewModel — @Observable + @MainActor
@Observable
@MainActor
public final class SignInViewModel {
    public private(set) var state = SignInState()
    private let signIn: SignInUseCase

    public init(signIn: SignInUseCase) {
        self.signIn = signIn
    }

    public func updateEmail(_ value: String) { state.email = value }

    public func submit() async {
        state.isLoading = true
        let result = await signIn(email: state.email, password: state.password)
        state.isLoading = false
        switch result {
        case .success: /* navigate */ break
        case .failure(let error): state.errorCode = error.code
        }
    }
}

// 3. View — takes ViewModel
public struct KHAuthView: View {
    @State private var viewModel: SignInViewModel
    public init(viewModel: SignInViewModel) { ... }
    public var body: some View { ... }
}
```

See `Packages/Features/KHAuth/Sources/KHAuth/KHAuthView.swift` for the working example.

---

## 🧪 Testing expectations

- **Unit tests:** ViewModels, UseCases, Mappers. XCTest framework.
- **Snapshot tests:** Every `KHDesignSystem` component + key screens via `pointfreeco/swift-snapshot-testing`.
- **UI tests:** Minimal — critical flows only (sign-in, generate, paywall).
- **Snapshot storage:** `__Snapshots__/` folders next to test files. Committed to repo.

---

## 🏃 Running locally

```bash
cd ios

# 1. Open workspace
open Kehdo.xcworkspace

# 2. Xcode auto-resolves Swift Packages on first open
# 3. Generate OpenAPI client (also runs as Swift Package plugin)
./Scripts/generate-openapi.sh

# 4. Build (⌘B) and run (⌘R) — picks iPhone 15 simulator
# 5. Tests (⌘U)

# CI:
xcodebuild test \
  -workspace Kehdo.xcworkspace \
  -scheme Kehdo \
  -destination 'platform=iOS Simulator,name=iPhone 15'
```

---

## 🚫 Do NOT

- ❌ Use CocoaPods — Swift Package Manager only
- ❌ Put business logic in SwiftUI Views — always in ViewModels/UseCases
- ❌ Hardcode colors, fonts, or strings — use `KHDesignSystem` + localized strings
- ❌ Use `UIApplication.shared` — use environment values where possible
- ❌ Use `DispatchQueue.main.async` — use `@MainActor` or `await MainActor.run`
- ❌ Use `completion: @escaping` callbacks — use `async/await`
- ❌ Force unwrap (`!`) in production code — always `guard let` or `if let`
- ❌ Add a Swift Package dependency without asking the user first
- ❌ Edit generated OpenAPI code — it regenerates on next build

---

## ✅ When adding a new feature

1. **API change?** Edit `/contracts/openapi/kehdo.v1.yaml` first, regenerate.
2. **Create use-case** in `KHDomain/Sources/<Feature>/UseCases/`.
3. **Implement repository** in `KHData/Sources/<Feature>RepositoryImpl.swift`.
4. **Create feature package** if new: `Packages/Features/KH<Feature>/`.
5. **Wire into navigation** in `App/Navigation/AppCoordinator.swift`.
6. **Tests:** ViewModel unit + snapshot for the main view.
7. **Test on simulator AND physical device** (TestFlight build if needed).
8. **Ask Claude for commit + push commands.** Claude does NOT push.

---

## 📱 Target devices

**Simulators (CI):**
- iPhone 15 (latest)
- iPhone SE 3rd gen (smallest screen)
- iPad Air (tablet layout validation)

**Physical devices (manual QA):**
- iPhone 15 Pro (latest, ProMotion)
- iPhone 13 (mid-range)
- iPad (tablet)

---

## 🔐 Keychain Sharing — Phase 2 prep

Day 1 setup includes Keychain Sharing entitlement with access group `$(AppIdentifierPrefix)app.kehdo.shared`. Tokens stored under this group so future extensions can read them without re-auth:

- iOS Share Sheet Extension (Phase 2)
- iOS Keyboard Extension (Phase 3)

App Group: `group.app.kehdo.shared` — for shared UserDefaults between app and extensions.

---

*iOS context version: 1.0*
