package tech.erben;

import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataMongoTest
@Testcontainers
class PersonRepositoryTest {

    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:8.2");

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

    @Test
    @DisplayName("Aufgabe 2: Abgeleitete Query nach firstname")
    void derivedQueryFindsPersonByFirstname() {
        Person saved = personRepository.save(randomPersonWithoutId());

        List<Person> found = personRepository.findPersonsByFirstname(saved.firstname());

        assertThat(found)
                .extracting(Person::id)
                .contains(saved.id());
    }

    @Test
    @DisplayName("Aufgabe 2: Abgeleitete Query nach lastname")
    void derivedQueryFindsPersonByLastname() {
        Person saved = personRepository.save(randomPersonWithoutId());

        List<Person> found = personRepository.findPersonsByLastname(saved.lastname());

        assertThat(found)
                .extracting(Person::id)
                .contains(saved.id());
    }

    @Test
    @DisplayName("Aufgabe 3: Person mit drei Phonenumbers speichern und laden")
    void personIsSavedAndLoadedWithThreePhonenumbers() {
        Person person = new Person(
                null,
                faker.name().firstName(),
                faker.name().lastName(),
                faker.timeAndDate().birthday(18, 80),
                List.of(
                        new Phonenumber(49, 15123456),
                        new Phonenumber(1, 2025550112),
                        new Phonenumber(44, 442071234)
                )
        );

        Person saved = personRepository.save(person);
        Person reloaded = personRepository.findById(saved.id()).orElseThrow();

        assertThat(reloaded.phonenumbers())
                .extracting(Phonenumber::countryCode, Phonenumber::number)
                .containsExactly(
                        tuple(49, 15123456),
                        tuple(1, 2025550112),
                        tuple(44, 442071234)
                );
    }

    private Person randomPersonWithoutId() {
        return new Person(
                null,
                faker.name().firstName(),
                faker.name().lastName(),
                faker.timeAndDate().birthday(18, 80)
        );
    }

    @SpringBootConfiguration
    @Import(DataConfiguration.class)
    static class TestConfig {
    }
}
