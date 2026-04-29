# CLAUDE.md — Android Workstream

> Loaded **in addition to** the root `/CLAUDE.md`.
> Read the root file first for universal rules (git, brand, architecture, commits).
> This file holds Android-specific context.

---

## 🎯 Scope

This directory holds the kehdo Android app. Open `android/` (not the repo root) in Android Studio Hedgehog 2023.1+.

- **Bundle ID:** `app.kehdo.android` (debug suffix: `.debug`)
- **Min SDK:** 26 (Android 8.0) — covers ~95% of devices
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **JDK:** 17 (toolchain)

---

## 🧱 Tech stack — locked

| Layer | Choice | Version | Reason |
|-------|--------|---------|--------|
| Language | Kotlin | 1.9.22 | Modern, coroutines-native |
| UI | Jetpack Compose | BOM 2024.02 | Declarative, future of Android |
| Design system | Material 3 | 1.2.0 | Standard, accessible, themed |
| DI | Hilt | 2.50 | Standard, low boilerplate |
| Async | Coroutines + Flow | 1.7.3 | Structured concurrency |
| Network | Retrofit + OkHttp | 2.11 / 4.12 | Battle-tested |
| Persistence | Room | 2.6.1 | Offline-first history |
| Prefs | DataStore | 1.0.0 | Type-safe, reactive |
| Secure storage | EncryptedSharedPreferences | 1.1.0-alpha06 | Hardware-backed |
| Image loading | Coil | 2.5.0 | Kotlin-first |
| Background work | WorkManager | 2.9.0 | Retry + constraints |
| OCR (fallback) | Google ML Kit | 16.0 | On-device for Pro users |
| Auth | Firebase Auth | BOM 32.7 | Google sign-in + email |
| Analytics | Mixpanel | 7.5.0 | Product analytics |
| Crash reports | Sentry | 7.3.0 | Error tracking |
| Testing | JUnit 4 + MockK + Turbine | — | Idiomatic Kotlin testing |
| UI testing | Compose Test + Paparazzi | — | Snapshot tests |
| Build | Gradle KTS | 8.5 | Modern build scripts |

All versions live in `gradle/libs.versions.toml`. **Never inline version strings in `build.gradle.kts` files.**

---

## 📂 Module structure (15 modules)

```
android/
├── app/                       :app — entry point, builds the APK
├── build-logic/               convention plugins (enforced architecture)
│   └── convention/
│       ├── AndroidApplicationConventionPlugin.kt
│       ├── AndroidLibraryConventionPlugin.kt
│       ├── AndroidComposeConventionPlugin.kt
│       ├── AndroidHiltConventionPlugin.kt
│       ├── AndroidFeatureConventionPlugin.kt
│       └── JvmLibraryConventionPlugin.kt
├── core/
│   ├── core-common/           Result types, KehdoError, extensions
│   ├── core-ui/               AuroraColors, KehdoTheme, components
│   ├── core-network/          OkHttp + interceptors + cert pinning
│   ├── core-network-generated/ ★ Retrofit client from OpenAPI (gitignored)
│   ├── core-database/         Room DB + DAOs
│   ├── core-datastore/        EncryptedSharedPreferences wrapper
│   ├── core-analytics/        Mixpanel facade (no-op in debug)
│   └── core-testing/          fakes, test rules, Compose test utils
├── domain/                    pure Kotlin, no Android deps
│   ├── domain-auth/
│   ├── domain-conversation/
│   └── domain-user/
├── data/                      implements domain repository protocols
│   ├── data-auth/
│   ├── data-conversation/
│   └── data-user/
└── feature/                   one module per screen cluster
    ├── feature-onboarding/
    ├── feature-auth/
    ├── feature-home/
    ├── feature-upload/
    ├── feature-reply/
    ├── feature-history/
    ├── feature-profile/
    └── feature-paywall/
```

---

## 🔒 Dependency rules — enforced by convention plugins

| From | May depend on | May NOT depend on |
|------|---------------|-------------------|
| `:app` | All `:feature:*`, all `:core:*`, all `:data:*` | — |
| `:feature:*` | `:domain:*`, `:core:ui`, `:core:common` | Another `:feature:*`, any `:data:*` |
| `:data:*` | `:domain:*`, `:core:network`, `:core:database`, `:core:datastore` | Any `:feature:*`, another `:data:*` |
| `:domain:*` | `:core:common` only | Anything Android. Zero platform deps. |
| `:core:*` | Other `:core:*` rarely | `:feature:*`, `:data:*` |

**Violations fail the build.** The convention plugins enforce this at compile time.

---

## 🎨 Design system usage

All colors, typography, spacing come from `:core:ui`. These are **generated** from `/design/tokens/`. Never hardcode hex values.

```kotlin
// ❌ WRONG
Text("Hello", color = Color(0xFF9C5BFF))

// ✅ RIGHT
Text("Hello", color = AuroraColors.Purple)
```

```kotlin
// Theme usage
KehdoTheme {
    AuroraButton(text = "Get started", onClick = { ... })
}
```

---

## 🧠 State management — MVI-lite (locked)

Every feature follows this exact shape:

```kotlin
// 1. State — immutable data class
data class ReplyUiState(
    val isLoading: Boolean = false,
    val screenshot: Uri? = null,
    val replies: List<Reply> = emptyList(),
    val error: KehdoError? = null
)

// 2. Events — sealed interface
sealed interface ReplyEvent {
    data class ScreenshotPicked(val uri: Uri) : ReplyEvent
    data class ToneSelected(val tone: Tone) : ReplyEvent
    data object GenerateClicked : ReplyEvent
}

// 3. ViewModel reduces events
@HiltViewModel
class ReplyViewModel @Inject constructor(
    private val generateReplies: GenerateRepliesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ReplyUiState())
    val state: StateFlow<ReplyUiState> = _state.asStateFlow()

    fun onEvent(event: ReplyEvent) { /* reduce */ }
}

// 4. Screen — stateless Composable
@Composable
fun ReplyScreen(
    state: ReplyUiState,
    onEvent: (ReplyEvent) -> Unit
) { /* UI */ }

// 5. ScreenRoute — wires ViewModel to Screen
@Composable
fun ReplyScreenRoute() {
    val viewModel: ReplyViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReplyScreen(state = state, onEvent = viewModel::onEvent)
}
```

See `feature/feature-auth/SignInViewModel.kt` for a working example.

---

## 🧪 Testing standards

- **Unit tests:** ViewModels (with Turbine for Flow), UseCases, Mappers, Reducers. MockK for mocks.
- **Instrumentation tests:** Minimal — only for native Android integration (Keystore, Room, Intents).
- **Compose UI tests:** For critical flows (login, generate, paywall).
- **Snapshot tests:** Every `:core:ui` component. Use Paparazzi (JVM-only, no emulator).
- **Coverage minimum:** 80% on ViewModels and UseCases. 60% overall.

---

## 🏃 Running locally

```bash
cd android

# 1. Generate API client (runs on first build automatically)
./gradlew :core:network-generated:openApiGenerate

# 2. Build debug APK
./gradlew :app:assembleDebug

# 3. Install on emulator/device
./gradlew :app:installDebug

# 4. Unit tests (fast, all modules)
./gradlew testDebugUnitTest

# 5. Paparazzi snapshot tests
./gradlew :core:ui:verifyPaparazziDebug

# 6. Full check (lint + test)
./gradlew check
```

---

## 🚫 Do NOT

- ❌ Hardcode hex color values — use `AuroraColors` from `:core:ui`
- ❌ Hardcode strings — use `strings.xml` (translated to hi/es/pt)
- ❌ Call Retrofit from a Composable — use ViewModel + UseCase
- ❌ Put business logic in `:feature:*` — that's what `:domain:*` is for
- ❌ Add a new dependency in `libs.versions.toml` without asking the user first
- ❌ Use `Activity` outside `:app` — all screens are Composables
- ❌ Use `runBlocking` in production code — use `viewModelScope`
- ❌ Edit anything in `:core:network-generated` — it regenerates from OpenAPI
- ❌ Skip the convention plugin and apply Android plugins manually

---

## ✅ When adding a new feature

1. **API change?** Edit `/contracts/openapi/kehdo.v1.yaml` first, then regenerate.
2. **Create use-case** in the appropriate `:domain:*` module.
3. **Implement repository** in `:data:*` (only if backend endpoint exists).
4. **Create or extend feature module:** `:feature:<name>` with UiState, Event, ViewModel, Screen, ScreenRoute.
5. **Wire it up** in `:app`'s `RootNavGraph.kt`.
6. **Tests:** ViewModel unit, Compose UI for interactive flows, Paparazzi for novel UI.
7. **Verify on emulator AND physical device** (at least one of each).
8. **Ask Claude for the commit + push commands.** Claude does NOT push.

---

## 📱 Target devices

**Primary test devices (must work):**
- Pixel 6 (stock Android)
- Samsung Galaxy A-series (most common in India)
- Low-end device (Android Go or 2GB RAM) — for performance

**Firebase Test Lab (CI):**
- Pixel 6 (latest Android)
- Pixel 4 (older Android)
- Samsung A13 (popular budget device)

---

*Android context version: 1.0*
