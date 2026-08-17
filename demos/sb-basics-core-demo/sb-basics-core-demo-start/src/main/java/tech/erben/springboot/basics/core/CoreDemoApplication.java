package tech.erben.springboot.basics.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Demo. {@code @SpringBootApplication} aktiviert das
 * Component-Scanning ab diesem Package — solange die anderen Klassen keine
 * Stereotyp-Annotationen tragen, findet der Container hier aber nichts.
 */
@SpringBootApplication
public class CoreDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreDemoApplication.class, args);
    }
}
