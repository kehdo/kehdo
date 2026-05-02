package app.kehdo.domain.auth

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Re-fetches the current user from the backend (`GET /me`). Use from screens
 * that need fresh identity/profile data — e.g. the home screen on first
 * composition, or after a profile edit.
 *
 * On success, the [AuthRepository.currentUser] flow emits the refreshed user.
 * On `Unauthorized`, the repository clears local session state so the root
 * nav graph routes the user back to auth.
 */
class RefreshCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Outcome<User> = authRepository.refreshCurrentUser()
}
