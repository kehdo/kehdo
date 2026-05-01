package app.kehdo.backend.config;

import app.kehdo.backend.auth.jwt.JwtService;
import app.kehdo.backend.auth.web.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Phase-2 security configuration.
 *
 * <p>Stateless JWT-bearer authentication: no sessions, no CSRF cookie. The
 * {@link JwtAuthenticationFilter} runs ahead of
 * {@link UsernamePasswordAuthenticationFilter}, validates the
 * {@code Authorization: Bearer <jwt>} header (if present), and pushes the
 * principal into the security context. Endpoints listed in
 * {@code permitAll()} skip this gate; {@code anyRequest().authenticated()}
 * forces it on everything else.</p>
 *
 * <ul>
 *   <li>{@code GET /health}, {@code /actuator/{health,info,metrics,prometheus}}
 *       — public probes</li>
 *   <li>{@code POST /auth/signup}, {@code /auth/login}, {@code /auth/refresh}
 *       — public; the endpoints themselves authenticate the user</li>
 *   <li>{@code POST /auth/logout} — REQUIRES auth (uses access-token claims
 *       to identify which session to revoke)</li>
 *   <li>everything else — authenticated</li>
 * </ul>
 *
 * <p>Paths are relative to {@code server.servlet.context-path: /v1}.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** BCrypt cost 12 per {@code SECURITY.md}. */
    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/metrics",
                                "/actuator/prometheus")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/signup",
                                "/auth/login",
                                "/auth/refresh").permitAll()
                        // /auth/logout is intentionally NOT public — it identifies
                        // the session to revoke from the access-token claims.
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
