package app.kehdo.data.auth

import app.kehdo.core.common.Outcome
import app.kehdo.core.datastore.TokenStore
import app.kehdo.core.network.api.AuthApi
import app.kehdo.core.network.api.dto.AuthResponseDto
import app.kehdo.core.network.api.dto.SignInRequestDto
import app.kehdo.core.network.api.dto.SignUpRequestDto
import app.kehdo.core.network.auth.AccessTokenHolder
import app.kehdo.data.auth.mapper.ErrorMapper
import app.kehdo.data.auth.mapper.toDomain
import app.kehdo.data.auth.mapper.toUser
import app.kehdo.domain.auth.AuthRepository
import app.kehdo.domain.auth.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val tokenHolder: AccessTokenHolder,
    json: Json
) : AuthRepository {

    private val errorMapper = ErrorMapper(json)
    private val _currentUser = MutableStateFlow<User?>(null)

    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun signUp(email: String, password: String): Outcome<User> = call {
        authApi.signUp(SignUpRequestDto(email = email, password = password))
    }

    override suspend fun signIn(email: String, password: String): Outcome<User> = call {
        authApi.signIn(SignInRequestDto(email = email, password = password))
    }

    override suspend fun signInWithGoogle(idToken: String): Outcome<User> =
        Outcome.Failure(
            app.kehdo.core.common.KehdoError.Server(
                code = "NOT_IMPLEMENTED",
                message = "Google sign-in is not yet supported"
            )
        )

    override suspend fun refreshToken(): Outcome<Unit> {
        val refresh = tokenStore.getRefreshToken()
            ?: return Outcome.Failure(
                app.kehdo.core.common.KehdoError.Unauthorized()
            )
        return runCatching {
            authApi.refresh(
                app.kehdo.core.network.api.dto.RefreshRequestDto(refresh)
            )
        }.fold(
            onSuccess = { response ->
                persistSession(response)
                Outcome.success(Unit)
            },
            onFailure = { Outcome.Failure(errorMapper.toKehdoError(it)) }
        )
    }

    override suspend fun refreshCurrentUser(): Outcome<User> = runCatching {
        authApi.getCurrentUser()
    }.fold(
        onSuccess = { dto ->
            val user = dto.toDomain()
            _currentUser.value = user
            Outcome.success(user)
        },
        onFailure = {
            val error = errorMapper.toKehdoError(it)
            // 401 from /me means the user no longer exists (soft-deleted) or
            // the refresh chain finally gave up — either way, drop local
            // state so the root nav graph bounces back to auth.
            if (error is app.kehdo.core.common.KehdoError.Unauthorized) {
                clearSession()
            }
            Outcome.Failure(error)
        }
    )

    override suspend fun tryRestoreSession(): Boolean {
        val refresh = tokenStore.getRefreshToken() ?: return false
        return runCatching {
            authApi.refresh(
                app.kehdo.core.network.api.dto.RefreshRequestDto(refresh)
            )
        }.fold(
            onSuccess = { response ->
                persistSession(response)
                true
            },
            onFailure = {
                clearSession()
                false
            }
        )
    }

    override suspend fun signOut(): Outcome<Unit> {
        val outcome = runCatching { authApi.signOut() }.fold(
            onSuccess = { Outcome.success(Unit) },
            onFailure = { Outcome.Failure(errorMapper.toKehdoError(it)) }
        )
        // Always clear local state — even if the server call failed, the user
        // intends to be signed out and we must not keep stale tokens around.
        clearSession()
        return outcome
    }

    private suspend inline fun call(crossinline block: suspend () -> AuthResponseDto): Outcome<User> =
        runCatching { block() }.fold(
            onSuccess = { response ->
                persistSession(response)
                Outcome.success(response.toUser())
            },
            onFailure = { Outcome.Failure(errorMapper.toKehdoError(it)) }
        )

    private fun persistSession(response: AuthResponseDto) {
        tokenHolder.set(response.accessToken)
        tokenStore.saveRefreshToken(response.refreshToken)
        _currentUser.value = response.toUser()
    }

    private fun clearSession() {
        tokenHolder.clear()
        tokenStore.clear()
        _currentUser.value = null
    }
}
