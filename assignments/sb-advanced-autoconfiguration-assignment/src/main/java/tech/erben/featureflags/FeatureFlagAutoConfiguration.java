package tech.erben.featureflags;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@AutoConfiguration
@EnableConfigurationProperties(FeatureFlagProperties.class)
@ConditionalOnClass(FeatureFlagManager.class)
public class FeatureFlagAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock featureFlagClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "demo.feature-flags",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public FeatureFlagManager featureFlagManager(
            FeatureFlagProperties properties,
            Clock clock
    ) {
        return new FeatureFlagManager(properties, clock);
    }
}
