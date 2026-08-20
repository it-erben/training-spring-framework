package tech.erben.springboot.batch.operations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Gegenstück zum Batch-Artefakt aus Modul 18: Diese Anwendung startet und
 * bleibt stehen. Jobs laufen auf Anforderung über HTTP, nicht beim Hochfahren
 * — deshalb steht {@code spring.batch.job.enabled} auf {@code false}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BatchOperationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchOperationsApplication.class, args);
    }
}
