package tech.erben.springboot.basics.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Testing-Demo — eine kombinierte Buchhandlung aus
 * Controller (Modul 02), Service und Repository (Modul 03). Der
 * Produktivcode ist in der {@code -start}- und der {@code -finished}-
 * Variante identisch: In diesem Modul wird nicht die Anwendung live
 * gebaut, sondern ihre Tests.
 */
@SpringBootApplication
public class TestingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingDemoApplication.class, args);
    }
}
