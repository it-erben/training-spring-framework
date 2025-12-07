package tech.erben.reactive;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static tech.erben.reactive.PersonGenerator.randomPerson;

import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(PersonController.class)
class PersonControllerTest {

    @MockBean
    private PersonRepository personRepository;

    @Autowired
    private WebTestClient webClient;

    @Test
    void getPersonById() {
        Person person1 = randomPerson();
        person1.setId(ThreadLocalRandom.current().nextLong());
        when(this.personRepository.findAll()).thenReturn(Flux.just(person1));
        webClient
            .get()
            .uri("/people/{id}", person1.getId())
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Person.class);
        Mockito.verify(personRepository, times(1)).findById(person1.getId());
    }

    @Test
    void getPeople() {
        Person person1 = randomPerson();
        Person person3 = randomPerson();
        Person person2 = randomPerson();
        when(this.personRepository.findAll())
            .thenReturn(Flux.just(person1, person2, person3));
        webClient
            .get()
            .uri("/people")
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(Person.class)
            .contains(person1, person2, person3);
        Mockito.verify(personRepository, times(1)).findAll();
    }
}
