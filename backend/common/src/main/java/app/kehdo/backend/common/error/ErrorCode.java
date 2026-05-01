package app.kehdo.backend.common.error;

/**
 * Canonical error codes used in {@link ApiError#code()}.
 *
 * <p>Mirrors {@code contracts/errors/codes.yaml}. Keep in sync — when a new code
 * is added there, add a constant here. Codes are UPPER_SNAKE_CASE.</p>
 */
public final class ErrorCode {

    private ErrorCode() {}

    // 4xx — client errors
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";

    // Auth
    public static final String EMAIL_ALREADY_REGISTERED = "EMAIL_ALREADY_REGISTERED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String REFRESH_TOKEN_INVALID = "REFRESH_TOKEN_INVALID";

    // 5xx — server errors
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
