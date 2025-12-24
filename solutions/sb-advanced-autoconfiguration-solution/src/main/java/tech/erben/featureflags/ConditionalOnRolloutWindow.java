package tech.erben.featureflags;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(RolloutWindowCondition.class)
public @interface ConditionalOnRolloutWindow {
    String flag();
}
