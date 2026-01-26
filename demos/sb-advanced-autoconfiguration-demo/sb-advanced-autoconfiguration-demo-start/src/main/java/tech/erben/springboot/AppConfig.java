package tech.erben.springboot;

import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;

@Configuration
@PropertySources(
        {
                @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = false),
        }
)
public class AppConfig {

    @Bean
    @Conditional(TomcatOnClassPathCondition.class)
    public TomcatLauncher tomcatLauncher() {
        return new TomcatLauncher();
    }

    @Bean
    @Conditional(DataSourcePropertySetCondition.class)
    public DataSource dataSource(Environment environment) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Driver driver = (Driver) Class.forName(environment.getProperty("spring.jdbc.driver")).newInstance();
        var url = environment.getProperty("spring.jdbc.url");
        return new SimpleDriverDataSource(driver, url);
    }

    private static class DataSourcePropertySetCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return context.getEnvironment().containsProperty("spring.jdbc.url") && context.getEnvironment().containsProperty("spring.jdbc.driver");
        }
    }


    private static class TomcatOnClassPathCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            try {
                Class.forName("org.apache.catalina.startup.Tomcat");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

}
