import Foundation
import KHCore

/// Repository contract for authentication.
///
/// Implementations live in KHData (`AuthRepositoryImpl`).
/// Domain code (use-cases) depends on this protocol only.
public protocol AuthRepository: Sendable {
    func currentUser() async -> User?
    func signUp(email: String, password: String) async -> Outcome<User>
    func signIn(email: String, password: String) async -> Outcome<User>
    func signInWithGoogle(idToken: String) async -> Outcome<User>
    func refreshToken() async -> Outcome<Void>
    func signOut() async -> Outcome<Void>
}
