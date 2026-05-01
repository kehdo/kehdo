package app.kehdo.backend.api.error;

import app.kehdo.backend.common.error.ApiError;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import app.kehdo.backend.common.error.ErrorEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps every uncaught exception to the universal {@link ErrorEnvelope} shape.
 *
 * <p>Order from most specific to most generic: {@link ApiException} (typed
 * domain errors with their own code + status), framework exceptions
 * (validation, Spring Security, malformed JSON, 404), and a final
 * {@link Exception} fallback that always returns 500/INTERNAL_ERROR.</p>
 *
 * <p>Per {@code backend/CLAUDE.md}: never log request bodies or response
 * bodies. The handler logs the exception class + message at WARN/ERROR
 * level only.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorEnvelope> handleApi(ApiException ex) {
        log.warn("ApiException [{} {}]: {}", ex.getCode(), ex.getHttpStatus(), ex.getMessage());
        ApiError err = new ApiError(
                ex.getCode(),
                ex.getMessage(),
                MDC.get(TRACE_ID_KEY),
                ex.getDetails());
        return ResponseEntity.status(ex.getHttpStatus()).body(new ErrorEnvelope(err));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> details.put(fe.getField(), fe.getDefaultMessage()));

        ApiError err = new ApiError(
                ErrorCode.BAD_REQUEST,
                "Request validation failed.",
                MDC.get(TRACE_ID_KEY),
                details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorEnvelope(err));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorEnvelope> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.debug("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        ApiError err = new ApiError(
                ErrorCode.BAD_REQUEST,
                "Request body is missing or malformed.",
                MDC.get(TRACE_ID_KEY),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorEnvelope(err));
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorEnvelope> handleAuthn(AuthenticationException ex) {
        ApiError err = new ApiError(
                ErrorCode.UNAUTHORIZED,
                "Authentication required.",
                MDC.get(TRACE_ID_KEY),
                null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorEnvelope(err));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorEnvelope> handleAuthz(AccessDeniedException ex) {
        ApiError err = new ApiError(
                ErrorCode.FORBIDDEN,
                "Not allowed.",
                MDC.get(TRACE_ID_KEY),
                null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorEnvelope(err));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorEnvelope> handle404(NoHandlerFoundException ex) {
        ApiError err = new ApiError(
                ErrorCode.NOT_FOUND,
                "Endpoint not found.",
                MDC.get(TRACE_ID_KEY),
                null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorEnvelope(err));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleAnything(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiError err = new ApiError(
                ErrorCode.INTERNAL_ERROR,
                "Something went wrong. Please try again.",
                MDC.get(TRACE_ID_KEY),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorEnvelope(err));
    }
}
