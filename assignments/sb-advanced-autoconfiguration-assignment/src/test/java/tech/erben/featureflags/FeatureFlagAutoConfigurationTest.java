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
    void createsManagerWhenFeatureFlagsEnabled() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(FeatureFlagManager.class)
        );
    }

    @Test
    void skipsManagerWhenFeatureFlagsDisabled() {
        contextRunner
                .withPropertyValues("demo.feature-flags.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FeatureFlagManager.class));
    }

    @Test
    void evaluatesSimpleFlagWindow() {
        contextRunner
                .withPropertyValues(
                        "demo.feature-flags.flags.demo.enabled=true",
                        "demo.feature-flags.flags.demo.rollout-start=1970-01-01T00:00:00Z"
                )
                .run(context ->
                        assertThat(
                                context.getBean(FeatureFlagManager.class).isEnabled("demo")
                        )
                                .isTrue()
                );
    }
}
