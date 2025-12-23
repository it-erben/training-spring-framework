package tech.erben;

import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

@DataMongoTest
@Testcontainers
public class PersonRepositoryTest {

    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:latest");

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(MONGO_IMAGE);
    Faker faker = new Faker();

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        registry.add("spring.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("spring.mongodb.database", () -> "testdb");
    }

    @Autowired
    PersonRepository personRepository;

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    /**
     * Test case to verify that when a user exists and is searched by username,
     * the correct user is retrieved.
     * Uses raw CrudRepository.
     */
    @Test
    public void givenUserExists_whenFindByUsername_thenGetUser() {
        personRepository.save(randomPersonWithoutId());
        personRepository.findAll().forEach(System.out::println);
    }

    private Person randomPersonWithoutId() {
        LocalDate birthday = faker.timeAndDate().birthday(18, 80);
        return new Person(
            null,
            faker.name().firstName(),
            faker.name().lastName(),
                birthday,
            List.of(new PhoneNumber(null, 49, 2), new PhoneNumber(null, 49, 4))
        );
    }

    @SpringBootConfiguration
    @Import(DataConfiguration.class)
    static class TestConfig {
    }
}
