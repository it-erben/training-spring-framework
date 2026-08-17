package tech.erben.springboot.basics.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Stellt eine Fremdklasse als Bean bereit: {@link Clock} stammt aus dem JDK
 * und kann nicht mit {@code @Component} annotiert werden. Die
 * {@code @Bean}-Methode übernimmt stattdessen die Registrierung im
 * Container — und Tests könnten hier eine feste Uhr injizieren.
 */
@Configuration
public class ShippingConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
