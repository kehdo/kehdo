// KehdoApp.swift — kehdo iOS entry point
// See /CLAUDE.md and /ios/CLAUDE.md for conventions.

import SwiftUI
import KHDesignSystem

@main
struct KehdoApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
                .kehdoTheme()
        }
    }
}

/// Top-level view. Routes to onboarding / auth / home based on app state.
struct RootView: View {
    @State private var coordinator = AppCoordinator()

    var body: some View {
        // TODO: replace with NavigationStack + coordinator-driven destinations
        // once feature modules ship their views (Phase 1).
        VStack(spacing: 24) {
            Spacer()
            Text("kehdo")
                .font(.system(size: 64, weight: .bold))
                .foregroundStyle(AuroraColors.gradient)

            Text("Reply with quiet confidence.")
                .font(.system(size: 18))
                .foregroundStyle(AuroraColors.textDim)

            Spacer()

            AuroraButton("Get started") {
                coordinator.beginOnboarding()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
    }
}
