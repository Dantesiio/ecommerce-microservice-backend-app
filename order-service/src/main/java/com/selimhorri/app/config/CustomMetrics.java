package com.selimhorri.app.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for order-service
 * Exposes metrics at /actuator/prometheus
 */
@Component
public class CustomMetrics {

    private final Counter orderCreatedCounter;
    private final Counter orderCompletedCounter;
    private final Counter orderCancelledCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.orderCreatedCounter = Counter.builder("orders_created_total")
            .description("Total number of orders created")
            .tag("service", "order-service")
            .register(registry);

        this.orderCompletedCounter = Counter.builder("orders_completed_total")
            .description("Total number of orders completed")
            .tag("service", "order-service")
            .register(registry);

        this.orderCancelledCounter = Counter.builder("orders_cancelled_total")
            .description("Total number of orders cancelled")
            .tag("service", "order-service")
            .register(registry);
    }

    public void incrementOrderCreated() {
        orderCreatedCounter.increment();
    }

    public void incrementOrderCompleted() {
        orderCompletedCounter.increment();
    }

    public void incrementOrderCancelled() {
        orderCancelledCounter.increment();
    }
}
