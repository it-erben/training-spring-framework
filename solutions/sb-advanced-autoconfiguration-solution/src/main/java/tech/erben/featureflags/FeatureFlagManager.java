package tech.erben.featureflags;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

public class FeatureFlagManager {

    private final FeatureFlagProperties properties;
    private final Clock clock;

    public FeatureFlagManager(FeatureFlagProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public boolean isEnabled(String featureKey) {
        if (!properties.isEnabled()) {
            return false;
        }
        Map<String, FeatureFlagProperties.FlagConfig> flags = properties.getFlags();
        FeatureFlagProperties.FlagConfig flagConfig = flags.get(featureKey);
        if (flagConfig == null || !flagConfig.isEnabled()) {
            return false;
        }
        Instant now = clock.instant();
        Instant rolloutStart = flagConfig.getRolloutStart();
        Instant rolloutEnd = flagConfig.getRolloutEnd();
        if (rolloutStart != null && now.isBefore(rolloutStart)) {
            return false;
        }
        return rolloutEnd == null || !now.isAfter(rolloutEnd);
    }
}
