package app.kehdo.domain.auth

import app.kehdo.core.common.Outcome
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Outcome<Unit> = authRepository.signOut()
}
