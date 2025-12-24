package tech.erben.featureflags;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("demo.feature-flags")
public class FeatureFlagProperties {

    private boolean enabled = true;

    private Map<String, FlagConfig> flags = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, FlagConfig> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, FlagConfig> flags) {
        this.flags = flags;
    }

    public static class FlagConfig {

        private boolean enabled;
        private Instant rolloutStart;
        private Instant rolloutEnd;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Instant getRolloutStart() {
            return rolloutStart;
        }

        public void setRolloutStart(Instant rolloutStart) {
            this.rolloutStart = rolloutStart;
        }

        public Instant getRolloutEnd() {
            return rolloutEnd;
        }

        public void setRolloutEnd(Instant rolloutEnd) {
            this.rolloutEnd = rolloutEnd;
        }
    }
}
