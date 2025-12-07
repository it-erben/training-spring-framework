package tech.erben.springboot.datajpa;

import java.util.List;

import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DataJpaDemoApplication {

    private static Faker FAKER = new Faker();

    public static void main(String[] args) {
        SpringApplication.run(DataJpaDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(CustomerRepository repository) {
        return args -> {

            // Setup Data
            Customer c1 = generateCustomer();
            c1.getOrders().add(new Order(FAKER.beer().name()));
            c1.getOrders().add(new Order(FAKER.beer().name()));
            repository.save(c1);

            Customer c2 = generateCustomer();
            repository.save(c2);

            repository.save(generateCustomer());
            repository.save(generateCustomer());
            repository.save(generateCustomer());

            // Lesen
            System.out.println("Customers found with findAll():");
            repository
                .findAll()
                .forEach(c -> System.out.println(c.getFirstName() + " " + c.getLastName())
                );

            // Deep Dive: Proxy Class
            System.out.println("Repository Class: " + repository.getClass().getName());

            // Entity Graph
            System.out.println("--------------------------------");
            System.out.println("EntityGraph (Customers + Orders):");
            List<Customer> customers = repository.findWithOrdersByLastName(c1.getLastName());
            customers.forEach(c ->
                System.out.println(
                    c.getFirstName() + " has " + c.getOrders().size() + " orders"
                )
            );

            // Projections
            System.out.println("--------------------------------");
            System.out.println("Projections (NameOnly):");
            repository
                .findProjectionsByLastName(c1.getFirstName())
                .forEach(p -> System.out.println(p.getFirstName() + " " + p.getLastName())
                );

            // DTOs
            System.out.println("--------------------------------");
            System.out.println("DTOs (Los Angeles):");
            repository
                .findCustomerDtosByCity(c1.getAddress().getCity())
                .forEach(System.out::println);

            // Auditing
            System.out.println("--------------------------------");
            System.out.println(
                "Auditing (Created Date of first customer): " +
                customers.getFirst().getCreatedDate()
            );
        };
    }

    private static Customer generateCustomer() {
        return new Customer(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                new Address(FAKER.address().city(), FAKER.address().streetAddress())
        );
    }
}
