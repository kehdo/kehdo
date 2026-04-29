package app.kehdo.domain.auth

import app.kehdo.core.common.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for authentication.
 *
 * Implementations live in :data:auth.
 * Domain code (use-cases) depends on this interface only.
 */
interface AuthRepository {

    /** Reactive stream of the current user, or null if signed out. */
    val currentUser: Flow<User?>

    suspend fun signUp(email: String, password: String): Outcome<User>

    suspend fun signIn(email: String, password: String): Outcome<User>

    suspend fun signInWithGoogle(idToken: String): Outcome<User>

    suspend fun refreshToken(): Outcome<Unit>

    suspend fun signOut(): Outcome<Unit>
}
