package tech.erben.springboot;

import java.util.Objects;
import javax.sql.DataSource;
import org.h2.Driver;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
@PropertySources(
    {
        @PropertySource(
            value = "classpath:application.properties",
            ignoreResourceNotFound = true
        ),
        @PropertySource(
            value = "classpath:application-${spring.profiles.active}.properties",
            ignoreResourceNotFound = true
        ),
    }
)
public class DefaultConfig {

    @Bean
    @Conditional(TomcatOnClassPathCondition.class)
    public TomcatLauncher tomcatLauncher() {
        return new TomcatLauncher();
    }

    @Bean
    @Conditional(DataSourcePropertySetCondition.class)
    public DataSource dataSource(Environment environment)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        var driver = (Driver) Class
            .forName(environment.getProperty("spring.jdbc.driver"))
            .newInstance();
        var url = environment.getProperty("spring.jdbc.url");
        return new SimpleDriverDataSource(driver, Objects.requireNonNull(url));
    }
}
