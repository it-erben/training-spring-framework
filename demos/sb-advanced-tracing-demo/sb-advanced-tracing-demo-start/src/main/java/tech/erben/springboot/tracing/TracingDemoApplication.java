package tech.erben.springboot.tracing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tech.erben.springboot.tracing.model.Customer;
import tech.erben.springboot.tracing.model.CustomerOrder;
import tech.erben.springboot.tracing.repository.CustomerRepository;

import java.math.BigDecimal;

@SpringBootApplication
public class TracingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracingDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoData(CustomerRepository customerRepository) {
        return args -> {
            if (customerRepository.count() > 0) {
                return;
            }

            Customer acme = new Customer("Acme Bikes", "info@acme.test");
            acme.addOrder(new CustomerOrder("City Bike", new BigDecimal("499.00")));
            acme.addOrder(new CustomerOrder("Service Package", new BigDecimal("99.00")));
            customerRepository.save(acme);

            Customer mountain = new Customer("Mountain Sports", "sales@mountain.test");
            mountain.addOrder(new CustomerOrder("Mountain Bike", new BigDecimal("1299.00")));
            customerRepository.save(mountain);

            Customer studio = new Customer("Studio Coffee", "hello@studio.test");
            customerRepository.save(studio);
        };
    }
}
