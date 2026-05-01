package app.kehdo.core.network.api.dto

import kotlinx.serialization.Serializable

/**
 * Universal API error envelope per /contracts/openapi/kehdo.v1.yaml.
 * Codes are UPPER_SNAKE_CASE strings defined in /contracts/errors/codes.yaml.
 */
@Serializable
data class ErrorEnvelopeDto(
    val error: ErrorBodyDto
)

@Serializable
data class ErrorBodyDto(
    val code: String,
    val message: String,
    val traceId: String? = null
)
