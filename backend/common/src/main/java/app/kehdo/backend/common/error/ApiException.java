package app.kehdo.backend.common.error;

import java.util.Map;

/**
 * Typed exception that carries an UPPER_SNAKE_CASE error code and an HTTP
 * status. Caught by the global exception handler and serialized as the
 * universal {@link ErrorEnvelope}.
 *
 * <p>Throw subclasses of this rather than ad-hoc {@link RuntimeException}s
 * so the response always carries a stable {@code code} clients can branch
 * on.</p>
 */
public class ApiException extends RuntimeException {

    private final String code;
    private final int httpStatus;
    private final Map<String, Object> details;

    public ApiException(String code, int httpStatus, String message) {
        this(code, httpStatus, message, (Map<String, Object>) null);
    }

    public ApiException(String code, int httpStatus, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public ApiException(String code, int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
    public Map<String, Object> getDetails() { return details; }
}
