package app.kehdo.backend.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Map;

/**
 * The {@code error} payload inside the universal {@link ErrorEnvelope}.
 *
 * <p>Codes are UPPER_SNAKE_CASE and must come from {@code contracts/errors/codes.yaml}
 * — never invent inline. {@code traceId} is the Sleuth/OpenTelemetry trace id when
 * available so support can correlate to logs. {@code details} is an optional
 * structured payload (e.g. validation field errors).</p>
 */
@JsonInclude(Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        String traceId,
        Map<String, Object> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null, null);
    }

    public static ApiError of(String code, String message, String traceId) {
        return new ApiError(code, message, traceId, null);
    }
}
