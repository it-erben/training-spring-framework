package tech.erben;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Import(TestConfig.class)
public class CachingTest {

    Faker faker = Faker.instance();

    @MockBean
    PersonRepository personRepository;

    @Autowired
    PersonService personService;

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    void testCaching() {
        Person person = randomPersonWithId(UUID.randomUUID().toString());
        when(personRepository.save(Mockito.any())).thenReturn(person);
        when(personRepository.findById(eq(person.id()))).thenReturn(Optional.of(person));
        Optional<Person> cacheMiss = personService.findById(person.id());
        Optional<Person> cacheHit = personService.findById(person.id());
        assertTrue(cacheMiss.isPresent());
        assertTrue(cacheHit.isPresent());
        assertEquals(person.firstname(), cacheMiss.get().firstname());
        assertEquals(person.firstname(), cacheHit.get().firstname());
        verify(personRepository, times(1)).findById(person.id());

        Mockito.reset(personRepository);

        Person updated = person.withLastname(faker.name().lastName());
        personService.save(updated);
        when(personRepository.save(Mockito.any())).thenReturn(person);
        when(personRepository.findById(eq(updated.id())))
            .thenReturn(Optional.of(updated));
        cacheMiss = personService.findById(updated.id());
        cacheHit = personService.findById(updated.id());
        assertTrue(cacheMiss.isPresent());
        assertTrue(cacheHit.isPresent());
        assertEquals(updated.firstname(), cacheMiss.get().firstname());
        assertEquals(updated.firstname(), cacheHit.get().firstname());
        verify(personRepository, times(1)).findById(updated.id());
    }

    private Person randomPersonWithId(String id) {
        Date birthday = faker.date().birthday(18, 80);
        return new Person(id, faker.name().firstName(), faker.name().lastName());
    }
}
