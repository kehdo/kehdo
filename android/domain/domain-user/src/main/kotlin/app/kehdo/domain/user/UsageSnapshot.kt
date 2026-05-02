package app.kehdo.domain.user

/**
 * Daily reply quota state. Mirrors the backend `UsageResponse` from
 * `/contracts/openapi/kehdo.v1.yaml`. UI uses this for the upload-screen
 * footer ("3 of 5 replies left today, resets at 12:00 AM").
 *
 * @property dailyUsed how many replies the user has consumed today
 * @property dailyLimit cap for their plan (5 STARTER / 100 PRO / 999_999 UNLIMITED)
 * @property resetAtMillis epoch-millis when the counter resets (UTC midnight)
 */
data class UsageSnapshot(
    val dailyUsed: Int,
    val dailyLimit: Int,
    val resetAtMillis: Long
) {
    val remaining: Int get() = (dailyLimit - dailyUsed).coerceAtLeast(0)

    /** UNLIMITED plan uses a sentinel; never display the number. */
    val isUnlimited: Boolean get() = dailyLimit >= UNLIMITED_THRESHOLD

    companion object {
        const val UNLIMITED_THRESHOLD = 100_000
    }
}
