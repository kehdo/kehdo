package app.kehdo.domain.auth

/**
 * Domain model for an authenticated user.
 * Pure Kotlin — no Android, no networking, no persistence concerns.
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val plan: Plan,
    val quotaRemaining: Int,
    val quotaResetAt: Long
) {
    enum class Plan { FREE, PRO, UNLIMITED }
}
