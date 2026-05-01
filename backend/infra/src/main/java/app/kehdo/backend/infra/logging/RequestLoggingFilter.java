package app.kehdo.backend.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logs one line per request: method, path, status, duration, remote IP,
 * truncated user agent.
 *
 * <p>Per {@code backend/CLAUDE.md}: "Request logging: only request IDs,
 * never body content." This filter NEVER reads the request or response
 * body — doing so would either consume the stream (breaking downstream
 * handlers) or require body buffering, both of which we explicitly
 * avoid.</p>
 *
 * <p>Generates a request id, sets it in MDC under {@code traceId} so
 * downstream loggers and the {@code GlobalExceptionHandler}'s error
 * envelope pick it up.</p>
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String MDC_TRACE_ID = "traceId";
    private static final int USER_AGENT_LIMIT = 100;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        long start = System.nanoTime();
        String traceId = "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader("X-Request-Id", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            String ua = request.getHeader("User-Agent");
            if (ua != null && ua.length() > USER_AGENT_LIMIT) {
                ua = ua.substring(0, USER_AGENT_LIMIT) + "…";
            }
            log.info(
                    "{} {} -> {} ({}ms) ip={} ua={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    request.getRemoteAddr(),
                    ua);
            MDC.remove(MDC_TRACE_ID);
        }
    }
}
