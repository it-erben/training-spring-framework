package tech.erben.springboot.basics.recap.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Einstiegspunkt der Raumverwaltung. {@code @ConfigurationPropertiesScan}
 * registriert {@link RoomProperties} — der Component-Scan allein findet
 * {@code @ConfigurationProperties}-Typen nicht.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RoomAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomAdminApplication.class, args);
    }
}
