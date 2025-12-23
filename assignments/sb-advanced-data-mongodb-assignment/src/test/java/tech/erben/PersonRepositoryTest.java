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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@Testcontainers
public class PersonRepositoryTest {

    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:latest");

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(MONGO_IMAGE);
    Faker faker = new Faker();
    @Autowired
    PersonRepository personRepository;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        registry.add("spring.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("spring.mongodb.database", () -> "testdb");
    }

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    private Person randomPersonWithBirthday(LocalDate birthday) {
        return new Person(
                null,
                faker.name().firstName(),
                faker.name().lastName(),
                birthday
        );
    }

    /**
     * Test case to verify that when a user exists and is searched by username,
     * the correct user is retrieved.
     * Uses raw CrudRepository.
     */
    @Test
    public void givenUserExists_whenFindByUsername_thenGetUser() {
        Person appUser = randomPersonWithoutId();
        Person saved = personRepository.save(appUser);
        Optional<Person> foundUser = personRepository.findById(saved.id());
        assertTrue(foundUser.isPresent());
        assertThat(foundUser.get().firstname(), is(appUser.firstname()));
    }

    private Person randomPersonWithoutId() {
        LocalDate birthday = faker.timeAndDate().birthday(18, 80);
        return new Person(
                null,
                faker.name().firstName(),
                faker.name().lastName(),
                birthday
        );
    }

    private Person randomPersonWithId(String id) {
        LocalDate birthday = faker.timeAndDate().birthday(18, 80);
        return new Person(
                id,
                faker.name().firstName(),
                faker.name().lastName(),
                birthday
        );
    }

    @SpringBootConfiguration
    @Import(DataConfiguration.class)
    static class TestConfig {
    }
}
