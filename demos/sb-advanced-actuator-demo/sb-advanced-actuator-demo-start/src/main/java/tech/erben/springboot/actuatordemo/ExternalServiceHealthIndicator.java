package tech.erben.springboot.actuatordemo;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health
            .unknown()
            .withDetail("message", "external service check not implemented")
            .build();
    }
}
