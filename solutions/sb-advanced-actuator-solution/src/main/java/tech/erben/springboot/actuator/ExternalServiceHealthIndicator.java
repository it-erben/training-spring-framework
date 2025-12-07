package tech.erben.springboot.actuator;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

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
