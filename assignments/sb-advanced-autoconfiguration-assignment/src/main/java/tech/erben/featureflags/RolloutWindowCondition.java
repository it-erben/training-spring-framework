package tech.erben.featureflags;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

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
        return flag != null && !flag.isBlank();
        // TODO: Implementiere die Logik, die die Zeitfenster der Flag auswertet.
    }
}
