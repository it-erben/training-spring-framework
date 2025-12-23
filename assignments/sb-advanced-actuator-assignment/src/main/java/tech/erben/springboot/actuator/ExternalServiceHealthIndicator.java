package tech.erben.springboot.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // TODO: Simulierten externen Check implementieren
        return Health
            .unknown()
            .withDetail("message", "external service check not implemented")
            .build();
    }
}
