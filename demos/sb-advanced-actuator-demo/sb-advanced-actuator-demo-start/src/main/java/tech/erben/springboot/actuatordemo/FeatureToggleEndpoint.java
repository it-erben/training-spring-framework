package tech.erben.springboot.actuatordemo;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Endpoint(id = "featureToggle")
public class FeatureToggleEndpoint {

    private final Map<String, Boolean> toggles = new ConcurrentHashMap<>();

    public FeatureToggleEndpoint() {
        toggles.put("newCheckout", Boolean.FALSE);
        toggles.put("externalService", Boolean.TRUE);
    }

    @ReadOperation
    public Map<String, Boolean> getFeatureStatus() {
        return toggles;
    }
}
