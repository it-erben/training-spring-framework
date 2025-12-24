package tech.erben.featureflags;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class RolloutWindowCondition implements Condition {

    @Override
    public boolean matches(
            ConditionContext context,
            AnnotatedTypeMetadata metadata
    ) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(
                ConditionalOnRolloutWindow.class.getName()
        );
        if (attributes == null) {
            return true;
        }
        String flag = (String) attributes.get("flag");
        if (flag == null || flag.isBlank()) {
            return false;
        }
        FeatureFlagProperties properties = bindProperties(context);
        if (!properties.isEnabled()) {
            return false;
        }
        FeatureFlagProperties.FlagConfig config = properties
                .getFlags()
                .get(flag);
        if (config == null || !config.isEnabled()) {
            return false;
        }
        Clock clock = resolveClock(context);
        Instant now = clock.instant();
        Instant start = config.getRolloutStart();
        Instant end = config.getRolloutEnd();
        if (start != null && now.isBefore(start)) {
            return false;
        }
        return end == null || !now.isAfter(end);
    }

    private FeatureFlagProperties bindProperties(ConditionContext context) {
        return Binder
                .get(context.getEnvironment())
                .bind(
                        "demo.feature-flags",
                        Bindable.of(FeatureFlagProperties.class)
                )
                .orElseGet(FeatureFlagProperties::new);
    }

    private Clock resolveClock(ConditionContext context) {
        return Optional
                .ofNullable(context.getBeanFactory())
                .map(factory ->
                        factory.getBeanProvider(Clock.class).getIfAvailable(Clock::systemUTC)
                )
                .orElseGet(Clock::systemUTC);
    }
}
