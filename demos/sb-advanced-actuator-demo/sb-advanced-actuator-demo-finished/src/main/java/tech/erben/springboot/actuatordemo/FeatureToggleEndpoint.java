package tech.erben.springboot.actuatordemo;

import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Endpoint(id = "featureToggle")
public class FeatureToggleEndpoint {

    private final Map<String, Boolean> toggles = new ConcurrentHashMap<>();
    private final ExternalServiceHealthIndicator externalServiceHealthIndicator;

    public FeatureToggleEndpoint(
        ExternalServiceHealthIndicator externalServiceHealthIndicator
    ) {
        this.externalServiceHealthIndicator = externalServiceHealthIndicator;
        toggles.put("newCheckout", Boolean.FALSE);
        toggles.put("externalService", Boolean.TRUE);
    }

    @ReadOperation
    public Map<String, Boolean> getFeatureStatus() {
        return Map.copyOf(toggles);
    }

    @WriteOperation
    public Map<String, Object> setFeatureStatus(
        @Selector String featureName,
        boolean enabled
    ) {
        toggles.put(featureName, enabled);
        if ("externalService".equals(featureName)) {
            externalServiceHealthIndicator.setExternalServiceAvailable(enabled);
        }
        return Map.of("feature", featureName, "enabled", enabled);
    }

    @DeleteOperation
    public Map<String, Boolean> removeFeature(@Selector String featureName) {
        toggles.remove(featureName);
        return Map.copyOf(toggles);
    }
}
