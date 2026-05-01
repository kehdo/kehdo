package app.kehdo.backend.auth.config;

import app.kehdo.backend.auth.jwt.JwtKeys;
import app.kehdo.backend.auth.jwt.JwtProperties;
import app.kehdo.backend.auth.jwt.JwtService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the auth-module beans (JWT signing keys, JwtService, etc.) into
 * the Spring application context.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AuthConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public JwtKeys jwtKeys(JwtProperties props) {
        return JwtKeys.load(props);
    }

    @Bean
    public JwtService jwtService(JwtProperties props, JwtKeys keys, Clock clock) {
        return new JwtService(props, keys, clock);
    }
}
