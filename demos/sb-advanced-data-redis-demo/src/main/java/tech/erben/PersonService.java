package tech.erben;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Cacheable(value = "persons")
    public Optional<Person> findById(String id) {
        return personRepository.findById(id);
    }

    @CacheEvict(value = "persons", key = "#person.id")
    public Person save(Person person) {
        return personRepository.save(person);
    }
}
