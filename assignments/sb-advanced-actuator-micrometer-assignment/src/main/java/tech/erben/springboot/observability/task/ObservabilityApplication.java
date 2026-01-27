package tech.erben.springboot.observability.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class ObservabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.save(new Product(null, "Keyboard", new BigDecimal("49.90"), 15));
            repository.save(new Product(null, "Mouse", new BigDecimal("19.90"), 40));
            repository.save(new Product(null, "Monitor", new BigDecimal("199.00"), 8));
        };
    }
}
