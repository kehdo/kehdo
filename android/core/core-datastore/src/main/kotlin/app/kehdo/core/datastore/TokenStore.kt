package app.kehdo.core.datastore

/**
 * Persistent store for the long-lived refresh token.
 *
 * Refresh tokens are 30-day TTL strings minted by the backend
 * (format: `rt_<64hex>`). They never appear in logs, analytics, or
 * crash reports — see :core:analytics scrub config.
 *
 * The short-lived access token lives in memory in :core:network
 * (AccessTokenHolder) and is re-issued via this refresh token on 401.
 */
interface TokenStore {
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clear()
}
