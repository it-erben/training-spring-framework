package tech.erben;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher.of;
import static org.springframework.data.domain.ExampleMatcher.StringMatcher.EXACT;
import static org.springframework.data.domain.ExampleMatcher.matching;

import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    /**
     * Saves and loads a user using the MongoTemplate.
     */
    @Test
    void givenUserExists_loadWithMongoTemplate() {
        Person appUser = randomPersonWithoutId();
        Person saved = mongoTemplate.save(appUser);
        Person found =
            this.mongoTemplate.findById(new ObjectId(saved.id()), Person.class);
        assertNotNull(found);
        assertThat(found.firstname(), is(appUser.firstname()));
    }

    /**
     * Using a native query, loads all users after a birthday cutoff using a Criteria query.
     */
    @Test
    void givenUserExists_nativeQuery() {
        Person personWithBirthdayAfterCutoff = randomPersonWithBirthday(
            LocalDate.of(2001, 1, 1)
        );
        personRepository.saveAll(
            List.of(
                personWithBirthdayAfterCutoff,
                randomPersonWithBirthday(LocalDate.of(1999, 1, 1))
            )
        );
        LocalDate date = LocalDate.of(2000, 1, 1);
        Query query = new Query();
        query.addCriteria(Criteria.where("birthday").gt(date));
        List<Person> people = mongoTemplate.find(query, Person.class);
        assertEquals(1, people.size());
        assertEquals(
            personWithBirthdayAfterCutoff.firstname(),
            people.getFirst().firstname()
        );
    }

    /**
     * Uses query by example to load a user.
     */
    @Test
    void testWithQbe() {
        Person personToLookFor = randomPersonWithoutId();
        personRepository.saveAll(List.of(personToLookFor, randomPersonWithoutId()));

        // first, we find with an exact matcher for firstname.
        // there are many different kinds of matchers available, e.g. prefix search and ignoring casing
        Example<Person> exactFirstNameExample = Example.of(
            personToLookFor,
            matching().withMatcher("firstname", of(EXACT))
        );
        List<Person> persons = personRepository.findAll(exactFirstNameExample);
        assertEquals(1, persons.size());
        assertEquals(personToLookFor.firstname(), persons.getFirst().firstname());

        // now, we use an any matcher that will take all properties into account
        Example<Person> matchingAnyExample = Example.of(
            personToLookFor,
            ExampleMatcher.matchingAny()
        );
        persons = personRepository.findAll(matchingAnyExample);
        assertEquals(1, persons.size());
        assertEquals(personToLookFor.firstname(), persons.getFirst().firstname());
        // there are other ExampleMatchers available, just have a look
    }

    @Test
    void testWithQueryByName() {
        Person personWithBirthdayAfterCutoff = randomPersonWithBirthday(
            LocalDate.of(2001, 1, 1)
        );
        personRepository.saveAll(
            List.of(
                personWithBirthdayAfterCutoff,
                randomPersonWithBirthday(LocalDate.of(1999, 1, 1))
            )
        );
        LocalDate date = LocalDate.of(2000, 1, 1);
        List<Person> found = personRepository.findPersonsByBirthdayAfter(date);

        assertEquals(1, found.size());
        assertEquals(
            personWithBirthdayAfterCutoff.firstname(),
            found.getFirst().firstname()
        );
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
