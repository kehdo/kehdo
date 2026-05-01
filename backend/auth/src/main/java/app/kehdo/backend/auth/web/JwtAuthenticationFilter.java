package app.kehdo.backend.auth.web;

import app.kehdo.backend.auth.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the {@code Authorization: Bearer <jwt>} header and pushes the
 * resulting principal into the Spring Security context.
 *
 * <p>Failure modes:</p>
 * <ul>
 *   <li>No Authorization header → pass through; downstream
 *       {@code .anyRequest().authenticated()} returns 401.</li>
 *   <li>Header present but malformed or expired → log at DEBUG, do not
 *       set context, let the chain return 401.</li>
 * </ul>
 *
 * <p>Authentication principal is the user UUID (string); the session UUID
 * is exposed via {@link #SESSION_ID_ATTRIBUTE} on the request so handlers
 * (notably {@code /auth/logout}) can revoke the caller's session.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String SESSION_ID_ATTRIBUTE = "kehdo.sessionId";
    public static final String USER_ID_ATTRIBUTE = "kehdo.userId";

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            JwtService.ValidatedAccessToken validated = jwtService.validateAccess(token);

            var authority = new SimpleGrantedAuthority("ROLE_USER");
            var auth = new UsernamePasswordAuthenticationToken(
                    validated.userId().toString(), null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);

            request.setAttribute(USER_ID_ATTRIBUTE, validated.userId());
            request.setAttribute(SESSION_ID_ATTRIBUTE, validated.sessionId());
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            // Don't touch the security context — chain will treat it as anonymous
            // and the .authenticated() rule will short-circuit to 401.
        }

        chain.doFilter(request, response);
    }
}
