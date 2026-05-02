package app.kehdo.core.network.auth

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory holder for the short-lived access token (5-min TTL).
 *
 * The access token is intentionally NOT persisted — on cold start the
 * authenticator reads the long-lived refresh token from [TokenStore] and
 * mints a new access token via /auth/refresh. This minimises the window
 * an attacker has if disk is compromised.
 */
@Singleton
class AccessTokenHolder @Inject constructor() {

    private val token = AtomicReference<String?>(null)

    fun get(): String? = token.get()

    fun set(value: String?) {
        token.set(value)
    }

    fun clear() {
        token.set(null)
    }
}
