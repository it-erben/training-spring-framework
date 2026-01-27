package tech.erben.springboot.observability.task;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String PROMETHEUS_USERNAME = "prometheus";
    private static final String PROMETHEUS_PASSWORD = "{noop}prometheus-secret";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to("prometheus", "metrics"))
                        .hasRole("PROMETHEUS")
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails prometheusUser = User
                .withUsername(PROMETHEUS_USERNAME)
                .password(PROMETHEUS_PASSWORD)
                .roles("PROMETHEUS")
                .build();

        return new InMemoryUserDetailsManager(prometheusUser);
    }
}
