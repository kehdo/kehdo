package app.kehdo.backend.common.error;

/**
 * Universal error envelope returned by every 4xx/5xx response across the kehdo API.
 *
 * <p>Defined in the root {@code CLAUDE.md} and locked across backend, Android, iOS,
 * and web clients. Always serializes as:</p>
 *
 * <pre>{@code
 * {
 *   "error": {
 *     "code": "RATE_LIMIT_EXCEEDED",
 *     "message": "Daily limit of 5 replies reached. Upgrade to continue.",
 *     "traceId": "01HXY7F..."
 *   }
 * }
 * }</pre>
 */
public record ErrorEnvelope(ApiError error) {

    public static ErrorEnvelope of(String code, String message) {
        return new ErrorEnvelope(ApiError.of(code, message));
    }

    public static ErrorEnvelope of(String code, String message, String traceId) {
        return new ErrorEnvelope(ApiError.of(code, message, traceId));
    }
}
