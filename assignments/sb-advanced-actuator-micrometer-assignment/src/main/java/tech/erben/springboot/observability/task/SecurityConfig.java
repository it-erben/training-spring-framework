package tech.erben.springboot.observability.task;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Für eine einfache REST-API ist das hier in der Regel ausreichend.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // TODO: Absicherung von Prometheus (und optional metrics) ergänzen.
                        .anyRequest().permitAll()
                );

        // TODO: HTTP Basic aktivieren.
        return http.build();
    }
}
