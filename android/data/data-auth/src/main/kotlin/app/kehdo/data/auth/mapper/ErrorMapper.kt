package app.kehdo.data.auth.mapper

import app.kehdo.core.common.KehdoError
import app.kehdo.core.network.api.dto.ErrorEnvelopeDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

internal class ErrorMapper(private val json: Json) {

    fun toKehdoError(throwable: Throwable): KehdoError = when (throwable) {
        is IOException -> KehdoError.Network(throwable)
        is HttpException -> mapHttp(throwable)
        else -> KehdoError.Unknown(throwable)
    }

    private fun mapHttp(http: HttpException): KehdoError {
        if (http.code() == 401) return KehdoError.Unauthorized(http)
        val envelope = parseEnvelope(http) ?: return KehdoError.Server(
            code = "HTTP_${http.code()}",
            message = http.message()
        )
        return KehdoError.Server(envelope.error.code, envelope.error.message)
    }

    private fun parseEnvelope(http: HttpException): ErrorEnvelopeDto? {
        val body = http.response()?.errorBody()?.string().orEmpty()
        if (body.isBlank()) return null
        return runCatching { json.decodeFromString(ErrorEnvelopeDto.serializer(), body) }
            .getOrNull()
    }
}
