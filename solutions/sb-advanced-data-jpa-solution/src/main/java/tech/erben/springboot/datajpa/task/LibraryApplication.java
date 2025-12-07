package tech.erben.springboot.datajpa.task;

import java.util.List;
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
            PublisherInfo manning = new PublisherInfo("Manning", "NY");
            PublisherInfo prentice = new PublisherInfo("Prentice Hall", "NJ");

            Book b1 = new Book(
                "Spring Boot in Action",
                "Craig Walls",
                "9781617292545",
                manning
            );
            b1.getReviews().add(new Review("Great book!", 5));
            repository.save(b1);

            repository.save(
                new Book("Spring in Action", "Craig Walls", "9781617294945", manning)
            );
            repository.save(
                new Book("Clean Code", "Robert C. Martin", "9780132350884", prentice)
            );

            System.out.println("--------------------------------");
            System.out.println("Books by Craig Walls:");
            repository
                .findByAuthor("Craig Walls")
                .forEach(b -> System.out.println(b.getTitle()));

            System.out.println("--------------------------------");
            System.out.println("EntityGraph (Book + Reviews):");
            List<Book> booksWithReviews = repository.findWithReviewsByTitle(
                "Spring Boot in Action"
            );
            booksWithReviews.forEach(b ->
                System.out.println(
                    b.getTitle() + " has " + b.getReviews().size() + " reviews"
                )
            );

            System.out.println("--------------------------------");
            System.out.println(
                "Auditing (Created At): " + booksWithReviews.get(0).getCreatedAt()
            );

            System.out.println("--------------------------------");
            System.out.println("Projections (Title & ISBN):");
            repository
                .findProjectionsByAuthor("Craig Walls")
                .forEach(p -> System.out.println(p.getTitle() + " [" + p.getIsbn() + "]")
                );

            System.out.println("--------------------------------");
            System.out.println("DTOs (Manning books):");
            repository
                .findDtosByPublisher("Manning")
                .forEach(dto -> System.out.println(dto));

            System.out.println("--------------------------------");
            System.out.println("Modifying Update (Manning -> Manning Publications):");
            repository.updatePublisherName("Manning", "Manning Publications");
            repository
                .findDtosByPublisher("Manning Publications")
                .forEach(dto -> System.out.println("Updated: " + dto));
        };
    }
}
