package tech.erben;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = MongoDBTestContainerConfig.class)
public class PersonRepositoryTest {

    Faker faker = Faker.instance();

    @Autowired
    MongoTemplate mongoTemplate;

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
        Person appUser = randomPersonWithoutId();
        Person saved = personRepository.save(appUser);
        Optional<Person> foundUser = personRepository.findById(saved.id());
        assertTrue(foundUser.isPresent());
        assertThat(foundUser.get().firstname(), is(appUser.firstname()));
    }

    private Person randomPersonWithBirthday(LocalDate birthday) {
        return new Person(
            null,
            faker.name().firstName(),
            faker.name().lastName(),
            birthday
        );
    }

    private Person randomPersonWithoutId() {
        Date birthday = faker.date().birthday(18, 80);
        return new Person(
            null,
            faker.name().firstName(),
            faker.name().lastName(),
            LocalDate.ofInstant(birthday.toInstant(), ZoneId.systemDefault())
        );
    }

    private Person randomPersonWithId(String id) {
        Date birthday = faker.date().birthday(18, 80);
        return new Person(
            id,
            faker.name().firstName(),
            faker.name().lastName(),
            LocalDate.ofInstant(birthday.toInstant(), ZoneId.systemDefault())
        );
    }
}
