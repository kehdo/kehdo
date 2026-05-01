package app.kehdo.core.network.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches `Authorization: Bearer <accessToken>` to every outgoing request,
 * unless the request already carries an Authorization header (e.g. /auth/login
 * itself) or the @Skip* tags are explicitly set.
 *
 * On 401 responses the [TokenRefreshAuthenticator] picks up and refreshes.
 */
class AuthInterceptor @Inject constructor(
    private val tokenHolder: AccessTokenHolder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header(HEADER_AUTH) != null) {
            return chain.proceed(original)
        }
        val token = tokenHolder.get() ?: return chain.proceed(original)
        val authed = original.newBuilder()
            .header(HEADER_AUTH, "Bearer $token")
            .build()
        return chain.proceed(authed)
    }

    private companion object {
        const val HEADER_AUTH = "Authorization"
    }
}
