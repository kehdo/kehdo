package app.kehdo.backend.api.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Public liveness probe for the kehdo API.
 *
 * <p>Distinct from Spring Actuator's {@code /actuator/health} (which is detailed
 * and includes downstream dependency checks). This endpoint is the simple
 * "is the API up?" signal that load balancers and external monitors use; it
 * never depends on Postgres or Redis being reachable.</p>
 */
@RestController
public class HealthController {

    private final String version;

    public HealthController(@Value("${spring.application.version:0.0.1-SNAPSHOT}") String version) {
        this.version = version;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", version, Instant.now().toString());
    }

    public record HealthResponse(String status, String version, String timestamp) {}
}
