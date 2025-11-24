package com.selimhorri.app.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for payment-service
 * Exposes metrics at /actuator/prometheus
 */
@Component
public class CustomMetrics {

    private final Counter paymentSuccessCounter;
    private final Counter paymentFailedCounter;
    private final Counter paymentPendingCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.paymentSuccessCounter = Counter.builder("payments_success_total")
            .description("Total successful payments")
            .tag("service", "payment-service")
            .register(registry);

        this.paymentFailedCounter = Counter.builder("payments_failed_total")
            .description("Total failed payments")
            .tag("service", "payment-service")
            .register(registry);

        this.paymentPendingCounter = Counter.builder("payments_pending_total")
            .description("Total pending payments")
            .tag("service", "payment-service")
            .register(registry);
    }

    public void incrementPaymentSuccess() {
        paymentSuccessCounter.increment();
    }

    public void incrementPaymentFailed() {
        paymentFailedCounter.increment();
    }

    public void incrementPaymentPending() {
        paymentPendingCounter.increment();
    }
}
