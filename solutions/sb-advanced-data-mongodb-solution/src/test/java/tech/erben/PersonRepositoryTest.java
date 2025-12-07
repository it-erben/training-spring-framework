package tech.erben;

import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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
        mongoTemplate.save(randomPersonWithoutId());
        mongoTemplate.getCollection("person").find().forEach(System.out::println);
    }

    private Person randomPersonWithoutId() {
        Date birthday = faker.date().birthday(18, 80);
        return new Person(
            null,
            faker.name().firstName(),
            faker.name().lastName(),
            LocalDate.ofInstant(birthday.toInstant(), ZoneId.systemDefault()),
            List.of(new PhoneNumber(null, 49, 2), new PhoneNumber(null, 49, 4))
        );
    }
}
