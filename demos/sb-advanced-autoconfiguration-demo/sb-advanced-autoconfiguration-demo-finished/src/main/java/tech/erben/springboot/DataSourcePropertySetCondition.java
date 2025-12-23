package tech.erben.springboot;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DataSourcePropertySetCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return (
            context.getEnvironment().containsProperty("spring.jdbc.url") &&
            context.getEnvironment().containsProperty("spring.jdbc.driver")
        );
    }
}
