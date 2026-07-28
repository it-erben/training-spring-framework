package tech.erben.springboot.basics.operations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Betriebs-Demo. Die Anwendung selbst ist bewusst
 * klein — im Mittelpunkt stehen die Actuator-Endpoints und das, was
 * Spring Boot fuer den Betrieb mitbringt, ohne dass hier eine einzige
 * Zeile Betriebscode steht.
 */
@SpringBootApplication
public class OperationsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperationsDemoApplication.class, args);
    }
}
