package app.kehdo.domain.user

import app.kehdo.core.common.Outcome

/**
 * Repository contract for user-scoped reads. Backed by GET /v1/me/usage
 * and (later) profile reads. Implementation lives in :data:user.
 */
interface UserRepository {

    /** Daily reply quota state for the authenticated user. */
    suspend fun getUsage(): Outcome<UsageSnapshot>
}
