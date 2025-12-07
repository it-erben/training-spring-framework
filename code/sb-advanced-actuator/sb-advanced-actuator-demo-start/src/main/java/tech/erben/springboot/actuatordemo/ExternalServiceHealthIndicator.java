package tech.erben.springboot.actuatordemo;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
