package tech.erben.springboot;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonLoggingService personAuditService;

    public PersonService(
        PersonRepository personRepository,
        PersonLoggingService personLoggingService
    ) {
        this.personRepository = personRepository;
        this.personAuditService = personLoggingService;
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
        personAuditService.recordCreated(saved);
        return saved;
    }
}
