package tech.erben.springboot.basics.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Demo. {@code @SpringBootApplication} aktiviert das
 * Component-Scanning ab diesem Package: Alle mit Stereotyp-Annotationen
 * versehenen Klassen werden als Beans im Spring-Container registriert.
 */
@SpringBootApplication
public class CoreDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreDemoApplication.class, args);
    }
}
