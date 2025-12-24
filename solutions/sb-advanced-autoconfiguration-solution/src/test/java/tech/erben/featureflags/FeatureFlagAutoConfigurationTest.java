package tech.erben.featureflags;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureFlagAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(FeatureFlagAutoConfiguration.class)
            );

    @Test
    void createsManagerWhenEnabled() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(FeatureFlagManager.class)
        );
    }

    @Test
    void skipsManagerWhenDisabled() {
        contextRunner
                .withPropertyValues("demo.feature-flags.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FeatureFlagManager.class));
    }

    @Test
    void createsProbeWithinRolloutWindow() {
        contextRunner
                .withPropertyValues(
                        "demo.feature-flags.flags.audit-log.enabled=true",
                        "demo.feature-flags.flags.audit-log.rollout-start=1970-01-01T00:00:00Z",
                        "demo.feature-flags.flags.audit-log.rollout-end=2070-01-01T00:00:00Z"
                )
                .run(context -> assertThat(context).hasSingleBean(FlagProbe.class));
    }

    @Test
    void skipsProbeOutsideRolloutWindow() {
        contextRunner
                .withPropertyValues(
                        "demo.feature-flags.flags.audit-log.enabled=true",
                        "demo.feature-flags.flags.audit-log.rollout-start=2070-01-01T00:00:00Z",
                        "demo.feature-flags.flags.audit-log.rollout-end=2071-01-01T00:00:00Z"
                )
                .run(context -> assertThat(context).doesNotHaveBean(FlagProbe.class));
    }
}
