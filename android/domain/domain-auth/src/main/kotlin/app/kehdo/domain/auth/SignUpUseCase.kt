package app.kehdo.domain.auth

import app.kehdo.core.common.Outcome
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Outcome<User> {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
        return authRepository.signUp(email.trim().lowercase(), password)
    }
}
