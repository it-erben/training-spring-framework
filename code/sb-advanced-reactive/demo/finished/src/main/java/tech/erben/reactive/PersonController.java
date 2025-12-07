package tech.erben.reactive;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/people")
public class PersonController {

    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping
    public Flux<Person> getAllPeople() {
        return personRepository.findAll();
    }

    @PostMapping
    public Mono<Person> createPerson(@RequestBody Person person) {
        return personRepository.save(person);
    }

    @GetMapping("/{id}")
    public Mono<Person> getPersonById(@PathVariable("id") Long id) {
        return personRepository.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<Person> updatePerson(@PathVariable Long id, @RequestBody Person person) {
        return personRepository
            .findById(id)
            .flatMap(existingPerson -> {
                existingPerson.setFirstName(person.getFirstName());
                existingPerson.setLastName(person.getLastName());
                return personRepository.save(existingPerson);
            })
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Invalid person id: " + id))
            );
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deletePerson(@PathVariable Long id) {
        return personRepository.deleteById(id);
    }
}
