package tech.erben.springboot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
public class OperatingSystemConfiguration {

    @Bean
    @Conditional(MacCondition.class)
    public OperatingSystem macOperatingSystem() {
        return new OperatingSystem.MacOperatingSystem();
    }

    @Bean
    @Conditional(LinuxCondition.class)
    public OperatingSystem linuxOperatingSystem() {
        return new OperatingSystem.LinuxOperatingSystem();
    }

    @Bean
    @Conditional(WindowsCondition.class)
    public OperatingSystem windowsOperatingSystem() {
        return new OperatingSystem.WindowsOperatingSystem();
    }

    public static class MacCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return System.getProperty("os.name").toLowerCase().contains("mac");
        }
    }

    public static class LinuxCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return System.getProperty("os.name").toLowerCase().contains("linux");
        }
    }

    public static class WindowsCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return System.getProperty("os.name").toLowerCase().contains("windows");
        }
    }
}
