package app.kehdo.domain.auth

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Sign in with email + password.
 *
 * Use-cases are single-purpose, suspend, return Outcome<T>.
 * They contain orchestration logic only — no HTTP, no DB.
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Outcome<User> {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
        return authRepository.signIn(email.trim().lowercase(), password)
    }
}
