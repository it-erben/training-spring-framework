package tech.erben;

import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@Import(TestConfig.class)
public class CachingTest {

    Faker faker = new Faker();

    @MockitoBean
    PersonRepository personRepository;

    @Autowired
    PersonService personService;

    @Autowired
    CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    void testCaching() {
        assertInstanceOf(RedisCacheManager.class, cacheManager, "Expected RedisCacheManager but got " + cacheManager.getClass().getName());
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
        return new Person(id, faker.name().firstName(), faker.name().lastName());
    }
}
