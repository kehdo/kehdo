package app.kehdo.data.user

import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.core.network.api.AuthApi
import app.kehdo.core.network.api.dto.UsageResponseDto
import app.kehdo.domain.user.UsageSnapshot
import app.kehdo.domain.user.UserRepository
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit-backed [UserRepository] implementation. Currently exposes
 * only the daily quota endpoint (`GET /v1/me/usage`); profile reads still
 * flow through the auth feature module's existing AuthApi.getCurrentUser
 * path.
 */
@Singleton
class RealUserRepository @Inject constructor(
    private val api: AuthApi
) : UserRepository {

    override suspend fun getUsage(): Outcome<UsageSnapshot> = runNetwork {
        api.getCurrentUsage().toDomain()
    }

    private inline fun <T> runNetwork(block: () -> T): Outcome<T> = try {
        Outcome.success(block())
    } catch (io: IOException) {
        // Connection refused, DNS, timeout — anything not making it to a
        // response. Distinct from HTTP errors (HttpException below).
        Outcome.failure(KehdoError.Network(io))
    } catch (http: HttpException) {
        Outcome.failure(httpToDomain(http))
    } catch (t: Throwable) {
        Outcome.failure(KehdoError.Unknown(t))
    }

    private fun httpToDomain(e: HttpException): KehdoError = when (e.code()) {
        401 -> KehdoError.Unauthorized(e)
        else -> KehdoError.Server(code = "HTTP_${e.code()}", message = e.message())
    }

    private fun UsageResponseDto.toDomain(): UsageSnapshot = UsageSnapshot(
        dailyUsed = dailyUsed,
        dailyLimit = dailyLimit,
        resetAtMillis = parseInstantOrNow(resetAt)
    )

    private fun parseInstantOrNow(iso8601: String): Long = try {
        Instant.parse(iso8601).toEpochMilli()
    } catch (_: DateTimeParseException) {
        System.currentTimeMillis()
    }
}
