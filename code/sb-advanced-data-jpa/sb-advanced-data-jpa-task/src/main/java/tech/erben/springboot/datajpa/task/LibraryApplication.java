package tech.erben.springboot.datajpa.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(BookRepository repository) {
        return args -> {
            Book b1 = new Book(
                "Spring Boot in Action",
                "Craig Walls",
                "9781617292545"
            );
            repository.save(b1);
            repository.save(
                new Book("Spring in Action", "Craig Walls", "9781617294945")
            );
            repository.save(
                new Book("Clean Code", "Robert C. Martin", "9780132350884")
            );

            System.out.println("--------------------------------");
            System.out.println("Books by Craig Walls:");
            repository
                .findByAuthor("Craig Walls")
                .forEach(b -> System.out.println(b.getTitle()));

        };
    }
}
