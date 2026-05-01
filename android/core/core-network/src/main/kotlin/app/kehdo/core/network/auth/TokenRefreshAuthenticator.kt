package app.kehdo.core.network.auth

import app.kehdo.core.datastore.TokenStore
import app.kehdo.core.network.api.AuthApi
import app.kehdo.core.network.api.dto.RefreshRequestDto
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles 401 responses by exchanging the stored refresh token for a new
 * access token, then retrying the original request exactly once.
 *
 * If the refresh fails — token expired, server says no — the authenticator
 * gives up: clears both stores and returns null so OkHttp surfaces the 401
 * to the caller. The repository layer then emits a SignedOut event.
 *
 * AuthApi is supplied via [Lazy] to break the dependency cycle:
 *   OkHttpClient → Authenticator → AuthApi → Retrofit → OkHttpClient.
 */
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val tokenHolder: AccessTokenHolder,
    private val authApi: Lazy<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) {
            // Already retried once; don't loop.
            return null
        }
        val refreshToken = tokenStore.getRefreshToken() ?: return null

        val refreshed = runCatching {
            runBlocking { authApi.get().refresh(RefreshRequestDto(refreshToken)) }
        }.getOrElse {
            // Refresh failed — clear state and let the 401 propagate.
            tokenStore.clear()
            tokenHolder.clear()
            return null
        }

        tokenHolder.set(refreshed.accessToken)
        tokenStore.saveRefreshToken(refreshed.refreshToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.accessToken}")
            .build()
    }
}
