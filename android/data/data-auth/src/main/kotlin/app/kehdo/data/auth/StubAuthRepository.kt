package app.kehdo.data.auth

import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.domain.auth.AuthRepository
import app.kehdo.domain.auth.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder for the scaffold PR. Real Retrofit-backed implementation lands
 * in PR 2 (`feat/and/auth-flow`); until then this keeps the Hilt graph
 * compilable and any caller surfaces a clear NOT_IMPLEMENTED error.
 */
@Singleton
class StubAuthRepository @Inject constructor() : AuthRepository {

    override val currentUser: Flow<User?> = flowOf(null)

    override suspend fun signUp(email: String, password: String): Outcome<User> =
        Outcome.Failure(notImplemented)

    override suspend fun signIn(email: String, password: String): Outcome<User> =
        Outcome.Failure(notImplemented)

    override suspend fun signInWithGoogle(idToken: String): Outcome<User> =
        Outcome.Failure(notImplemented)

    override suspend fun refreshToken(): Outcome<Unit> =
        Outcome.Failure(notImplemented)

    override suspend fun signOut(): Outcome<Unit> =
        Outcome.Failure(notImplemented)

    private val notImplemented: KehdoError =
        KehdoError.Server("NOT_IMPLEMENTED", "Auth pipeline not wired yet")
}
