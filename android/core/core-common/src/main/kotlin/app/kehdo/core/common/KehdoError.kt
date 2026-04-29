package app.kehdo.core.common

/**
 * Domain-level error type. Mirrors the API ErrorEnvelope from /contracts/.
 *
 * UI layers map these to user-facing strings via :core:ui's error mapper,
 * which reads localized messages from /design/copy/<lang>.json.
 */
sealed class KehdoError(
    open val code: String,
    open val cause: Throwable? = null
) {
    /** Network is unreachable or timed out. */
    data class Network(override val cause: Throwable? = null) :
        KehdoError("NETWORK_ERROR", cause)

    /** Authentication required or token expired. */
    data class Unauthorized(override val cause: Throwable? = null) :
        KehdoError("UNAUTHORIZED", cause)

    /** User has hit their daily quota. */
    data class RateLimit(val limit: Int, val resetAt: Long) :
        KehdoError("RATE_LIMIT_EXCEEDED")

    /** OCR or speaker attribution failed. */
    data class ParsingFailed(val reason: String) :
        KehdoError("PARSING_FAILED")

    /** LLM provider returned an error. */
    data class GenerationFailed(val reason: String) :
        KehdoError("GENERATION_FAILED")

    /** Server returned a code we don't specifically handle. */
    data class Server(override val code: String, val message: String) :
        KehdoError(code)

    /** Unknown / unexpected error. */
    data class Unknown(override val cause: Throwable? = null) :
        KehdoError("UNKNOWN_ERROR", cause)
}
