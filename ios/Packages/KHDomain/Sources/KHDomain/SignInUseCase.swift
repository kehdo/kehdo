import Foundation
import KHCore

/// Sign in with email + password.
///
/// Use-cases are single-purpose, async, return `Outcome<T>`.
/// They contain orchestration logic only — no networking or persistence.
public struct SignInUseCase: Sendable {
    private let authRepository: AuthRepository

    public init(authRepository: AuthRepository) {
        self.authRepository = authRepository
    }

    public func callAsFunction(email: String, password: String) async -> Outcome<User> {
        let trimmed = email.trimmingCharacters(in: .whitespaces).lowercased()
        guard !trimmed.isEmpty else {
            return .failure(.server(code: "INVALID_EMAIL", message: "Email cannot be blank"))
        }
        guard password.count >= 8 else {
            return .failure(.server(code: "INVALID_PASSWORD", message: "Password must be at least 8 characters"))
        }
        return await authRepository.signIn(email: trimmed, password: password)
    }
}
