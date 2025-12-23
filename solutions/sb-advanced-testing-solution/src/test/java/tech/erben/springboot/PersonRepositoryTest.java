package tech.erben.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void saveAndFindPerson() {
        Person person = new Person();
        person.setName("John Doe");
        person.setAge(30);

        personRepository.save(person);

        Optional<Person> foundPerson = personRepository.findById(person.getId());
        assertThat(foundPerson).isPresent();

        foundPerson.ifPresent(value -> {
            assertThat(value.getName()).isEqualTo(person.getName());
            assertThat(value.getAge()).isEqualTo(person.getAge());
        });
    }

    @Test
    void deletePerson() {
        Person person = new Person();
        person.setName("John Doe");
        person.setAge(30);

        personRepository.save(person);
        personRepository.delete(person);

        Optional<Person> foundPerson = personRepository.findById(person.getId());
        assertThat(foundPerson).isNotPresent();
    }
}
