package com.selimhorri.app.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for user-service
 * Exposes metrics at /actuator/prometheus
 */
@Component
public class CustomMetrics {

    private final Counter userCreatedCounter;
    private final Counter userLoginCounter;
    private final Counter userLoginFailedCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.userCreatedCounter = Counter.builder("users_created_total")
            .description("Total number of users created")
            .tag("service", "user-service")
            .register(registry);

        this.userLoginCounter = Counter.builder("users_login_success_total")
            .description("Total successful user logins")
            .tag("service", "user-service")
            .register(registry);

        this.userLoginFailedCounter = Counter.builder("users_login_failed_total")
            .description("Total failed user login attempts")
            .tag("service", "user-service")
            .register(registry);
    }

    public void incrementUserCreated() {
        userCreatedCounter.increment();
    }

    public void incrementLoginSuccess() {
        userLoginCounter.increment();
    }

    public void incrementLoginFailed() {
        userLoginFailedCounter.increment();
    }
}
