package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonRepositoryPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15-alpine"
    );

    @Autowired
    PersonRepository personRepository;

    @Test
    void savesAndLoadsWithRealPostgres() {
        Person person = new Person();
        person.setName("Container User");
        person.setAge(33);

        Person saved = personRepository.save(person);
        assertThat(saved.getId()).isNotNull();

        assertThat(personRepository.findById(saved.getId()))
            .isPresent()
            .get()
            .extracting(Person::getName)
            .isEqualTo("Container User");
    }
}
