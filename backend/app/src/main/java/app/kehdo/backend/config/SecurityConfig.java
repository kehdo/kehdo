package app.kehdo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Phase-2 security configuration.
 *
 * <p>Wires up the public surface of the API and BCrypt for password hashing.
 * Stateless: every request must carry credentials (or be a public route);
 * we never issue session cookies. JWT bearer-token validation lands in
 * {@code feat/be/auth-endpoints} once the auth domain is in place.</p>
 *
 * <ul>
 *   <li>{@code GET /health} — public liveness</li>
 *   <li>{@code /actuator/health}, {@code /actuator/info},
 *       {@code /actuator/metrics}, {@code /actuator/prometheus} — public ops</li>
 *   <li>{@code /auth/**} — public; the endpoints themselves authenticate</li>
 *   <li>everything else — denied (will start serving once a JWT filter
 *       is added ahead of this chain)</li>
 * </ul>
 *
 * <p>Note: paths are relative to {@code server.servlet.context-path: /v1}, so
 * "{@code /health}" matches {@code /v1/health} on the wire.</p>
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
