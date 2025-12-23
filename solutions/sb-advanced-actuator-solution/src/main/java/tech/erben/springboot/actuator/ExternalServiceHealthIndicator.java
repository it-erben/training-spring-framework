package tech.erben.springboot.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final AtomicBoolean externalServiceAvailable = new AtomicBoolean(true);

    @Override
    public Health health() {
        if (externalServiceAvailable.get()) {
            return Health
                .up()
                .withDetail("endpoint", "https://api.example.local")
                .withDetail("checkedAt", Instant.now())
                .build();
        }
        return Health
            .down()
            .withDetail("endpoint", "https://api.example.local")
            .withDetail("error", "Simulated outage")
            .withDetail("checkedAt", Instant.now())
            .build();
    }

    public void setExternalServiceAvailable(boolean available) {
        this.externalServiceAvailable.set(available);
    }
}
