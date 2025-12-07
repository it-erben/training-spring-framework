package erben.digital;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CustomServiceProperties.class)
public class CustomServiceAutoConfiguration {

    @Bean
    //@ConditionalOnProperty(prefix = "custom.service", name = "message", havingValue = "true")
    public CustomService customService(CustomServiceProperties properties) {
        return new CustomService(properties.getMessage());
    }
}
