import SwiftUI

/// The kehdo signature button — gradient fill, white text, subtle glow.
///
/// Use for primary CTAs only. Secondary actions should use `.bordered` style.
public struct AuroraButton: View {
    private let title: String
    private let action: () -> Void

    public init(_ title: String, action: @escaping () -> Void) {
        self.title = title
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: 52)
                .background(AuroraColors.gradient)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .shadow(color: AuroraColors.purple.opacity(0.4), radius: 16, y: 6)
        }
    }
}
