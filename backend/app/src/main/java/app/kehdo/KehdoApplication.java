package app.kehdo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * kehdo backend — Spring Boot entry point.
 *
 * <p>This is the only place with {@code @SpringBootApplication}. All feature modules
 * declare their {@code @Configuration} beans, and component scanning picks them up
 * via the base package {@code app.kehdo.backend}.</p>
 */
@SpringBootApplication(scanBasePackages = "app.kehdo.backend")
@EnableScheduling
public class KehdoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KehdoApplication.class, args);
    }
}
