package tech.erben.springboot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonLoggingService personLoggingService;

    public PersonService(
        PersonRepository personRepository,
        PersonLoggingService personLoggingService
    ) {
        this.personRepository = personRepository;
        this.personLoggingService = personLoggingService;
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Transactional
    public Person createPerson(String name, int age) {
        Person person = new Person();
        person.setName(name);
        person.setAge(age);
        Person saved = personRepository.save(person);
        personLoggingService.recordCreated(saved);
        return saved;
    }
}
