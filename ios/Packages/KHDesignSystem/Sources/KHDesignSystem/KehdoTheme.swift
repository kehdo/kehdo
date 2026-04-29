import SwiftUI

/// Root theme wrapper for the kehdo app.
///
/// Aurora is a dark-first design system. Apply once at the root view:
///
///     ContentView()
///         .kehdoTheme()
///
public struct KehdoTheme: ViewModifier {
    public func body(content: Content) -> some View {
        content
            .preferredColorScheme(.dark)
            .background(AuroraColors.canvas.ignoresSafeArea())
            .foregroundStyle(AuroraColors.text)
            .tint(AuroraColors.purple)
    }
}

public extension View {
    /// Apply the kehdo Aurora theme to this view tree.
    func kehdoTheme() -> some View {
        modifier(KehdoTheme())
    }
}
