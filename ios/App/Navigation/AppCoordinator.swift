import Foundation
import Observation

/// Top-level navigation coordinator.
///
/// As feature modules ship their views, the coordinator drives transitions
/// via a NavigationStack path. Keeps view code free of routing decisions.
@Observable
final class AppCoordinator {
    enum Route: Hashable {
        case onboarding
        case signIn
        case signUp
        case home
        case upload
        case reply(conversationId: String)
        case history
        case profile
        case paywall
    }

    var path: [Route] = []

    func beginOnboarding() {
        path = [.onboarding]
    }

    func showSignIn() {
        path.append(.signIn)
    }

    func showHome() {
        path = [.home]
    }

    func popToRoot() {
        path.removeAll()
    }
}
