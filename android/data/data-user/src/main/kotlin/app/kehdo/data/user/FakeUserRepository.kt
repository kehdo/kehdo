package app.kehdo.data.user

import app.kehdo.core.common.Outcome
import app.kehdo.domain.user.UsageSnapshot
import app.kehdo.domain.user.UserRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory fake of [UserRepository] used when the backend isn't running
 * locally (default — see `kehdo.useFakeData` in `gradle.properties`).
 * Returns a plausible STARTER-tier quota state (2 of 5 used) so the
 * upload-screen footer renders without surprising the developer.
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {

    override suspend fun getUsage(): Outcome<UsageSnapshot> {
        delay(80) // simulate a network round-trip so the loading state is visible
        return Outcome.success(
            UsageSnapshot(
                dailyUsed = 2,
                dailyLimit = 5,
                resetAtMillis = nextUtcMidnight()
            )
        )
    }

    private fun nextUtcMidnight(): Long {
        val msPerDay = 24L * 60 * 60 * 1000
        val now = System.currentTimeMillis()
        return (now / msPerDay + 1) * msPerDay
    }
}
