package tech.erben;

import net.datafaker.Faker;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataMongoTest
@Testcontainers
class PersonRepositoryTest {

    private static final DockerImageName MONGO_IMAGE =
            DockerImageName.parse("mongo:8.2");

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

    @Autowired
    MongoTemplate mongoTemplate;

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    void derivedQueryByLastnameReturnsMatch() {
        Person stored = personRepository.save(randomPersonWithoutId());

        List<Person> found = personRepository.findByLastname(stored.lastname());

        assertThat(found)
                .hasSize(1)
                .first()
                .extracting(Person::firstname, Person::lastname)
                .containsExactly(stored.firstname(), stored.lastname());
    }

    @Test
    void queryByExampleMatchesFirstname() {
        Person stored = personRepository.save(randomPersonWithoutId());
        ExampleMatcher matcher =
                ExampleMatcher.matching().withIgnorePaths("id", "birthday", "phonenumbers");
        Example<Person> example = Example.of(
                new Person(
                        null,
                        stored.firstname(),
                        null,
                        null,
                        null
                ),
                matcher
        );

        List<Person> matches = personRepository.findAll(example);

        assertThat(matches)
                .extracting(Person::firstname, Person::lastname)
                .contains(tuple(stored.firstname(), stored.lastname()));
    }

    @Test
    void mongoTemplateQueryFiltersByBirthday() {
        LocalDate cutoff = LocalDate.of(1990, 1, 1);
        Person younger = personRepository.save(randomPersonWithBirthday(cutoff.plusYears(10)));
        personRepository.save(randomPersonWithBirthday(cutoff.minusYears(20)));

        Query query = new Query(Criteria.where("birthday").gte(cutoff));
        List<Person> result = mongoTemplate.find(query, Person.class);

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Person::id)
                .isEqualTo(younger.id());
    }

    @Test
    void phonenumbersPersistAsEmbeddedDocuments() {
        Person person = new Person(
                null,
                faker.name().firstName(),
                faker.name().lastName(),
                faker.timeAndDate().birthday(18, 80),
                List.of(
                        new PhoneNumber(49, 1512345678),
                        new PhoneNumber(1, 2025550112),
                        new PhoneNumber(44, 770090123)
                )
        );

        Person saved = personRepository.save(person);
        Person reloaded = personRepository.findById(saved.id()).orElseThrow();

        assertThat(reloaded.phonenumbers())
                .hasSize(3)
                .extracting(PhoneNumber::countryCode, PhoneNumber::number)
                .containsExactly(
                        tuple(49, 1512345678),
                        tuple(1, 2025550112),
                        tuple(44, 770090123)
                );

        Document raw = mongoTemplate
                .getDb()
                .getCollection(mongoTemplate.getCollectionName(Person.class))
                .find()
                .first();

        assertThat(raw).isNotNull();
        List<Document> phoneDocs = raw.getList("phonenumbers", Document.class);
        assertThat(phoneDocs)
                .hasSize(3)
                .allSatisfy(doc ->
                        assertThat(doc).containsKeys("countryCode", "number"));
    }

    private Person randomPersonWithoutId() {
        return randomPersonWithBirthday(faker.timeAndDate().birthday(18, 80));
    }

    private Person randomPersonWithBirthday(LocalDate birthday) {
        return new Person(
            null,
            faker.name().firstName(),
            faker.name().lastName(),
                birthday,
                List.of(new PhoneNumber(49, faker.number().numberBetween(1000000, 9999999)))
        );
    }

    @SpringBootConfiguration
    @Import(DataConfiguration.class)
    static class TestConfig {
    }
}
