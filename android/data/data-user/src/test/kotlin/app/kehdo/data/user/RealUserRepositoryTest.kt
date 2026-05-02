package app.kehdo.data.user

import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.core.network.api.AuthApi
import app.kehdo.core.network.api.dto.UsageResponseDto
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant

class RealUserRepositoryTest {

    private val api: AuthApi = mockk()
    private val repository = RealUserRepository(api)

    @Test
    fun `getUsage maps DTO into UsageSnapshot with parsed reset timestamp`() = runTest {
        coEvery { api.getCurrentUsage() } returns UsageResponseDto(
            dailyUsed = 3,
            dailyLimit = 5,
            resetAt = "2026-05-04T00:00:00Z"
        )

        val result = repository.getUsage()

        assertThat(result).isInstanceOf(Outcome.Success::class.java)
        val usage = (result as Outcome.Success).value
        assertThat(usage.dailyUsed).isEqualTo(3)
        assertThat(usage.dailyLimit).isEqualTo(5)
        assertThat(usage.resetAtMillis)
            .isEqualTo(Instant.parse("2026-05-04T00:00:00Z").toEpochMilli())
    }

    @Test
    fun `getUsage maps IOException to KehdoError Network`() = runTest {
        coEvery { api.getCurrentUsage() } throws IOException("connect failed")

        val result = repository.getUsage()

        assertThat(result).isInstanceOf(Outcome.Failure::class.java)
        val error = (result as Outcome.Failure).error
        assertThat(error).isInstanceOf(KehdoError.Network::class.java)
    }

    @Test
    fun `getUsage maps 401 to KehdoError Unauthorized so refresh kicks in`() = runTest {
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        coEvery { api.getCurrentUsage() } throws HttpException(
            Response.error<Any>(401, errorBody)
        )

        val result = repository.getUsage()

        assertThat(result).isInstanceOf(Outcome.Failure::class.java)
        assertThat((result as Outcome.Failure).error).isInstanceOf(KehdoError.Unauthorized::class.java)
    }

    @Test
    fun `getUsage maps unhandled HTTP code to KehdoError Server with code prefix`() = runTest {
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        coEvery { api.getCurrentUsage() } throws HttpException(
            Response.error<Any>(503, errorBody)
        )

        val result = repository.getUsage()

        val error = (result as Outcome.Failure).error
        assertThat(error).isInstanceOf(KehdoError.Server::class.java)
        assertThat(error.code).isEqualTo("HTTP_503")
    }
}
