package app.kehdo.backend.infra.logging;

import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Infrastructure-wide servlet beans (request logging, future tracing,
 * future request-id propagation, etc.) that should run ahead of feature
 * filters.
 */
@Configuration
public class InfraConfig {

    @Bean
    public FilterRegistrationBean<Filter> requestLoggingFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>(new RequestLoggingFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }
}
