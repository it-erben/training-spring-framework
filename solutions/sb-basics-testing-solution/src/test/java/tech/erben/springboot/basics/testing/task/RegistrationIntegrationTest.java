package tech.erben.springboot.basics.testing.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lösung zu Aufgabe 3: Integrationstest. {@code RANDOM_PORT} startet die
 * komplette Anwendung mit echtem Tomcat und In-Memory-H2, das
 * {@link TestRestTemplate} schickt echte HTTP-Requests dagegen. Hier ist
 * nichts gemockt — der Kurs {@code SPRING-COMPACT} aus dem Seed hat genau
 * einen Platz, deshalb beweist erst die zweite Anmeldung, dass die erste
 * wirklich in der Datenbank gelandet ist.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class RegistrationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Anmeldung Ende-zu-Ende: letzter Platz 201, danach 409")
    void registersUntilCourseIsFull() {
        ResponseEntity<Void> first = restTemplate.postForEntity(
                "/api/courses/SPRING-COMPACT/participants",
                new RegistrationRequest("anna@example.com"), Void.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getHeaders().getLocation())
                .isEqualTo(URI.create("/api/courses/SPRING-COMPACT/participants"));

        ResponseEntity<Void> second = restTemplate.postForEntity(
                "/api/courses/SPRING-COMPACT/participants",
                new RegistrationRequest("ben@example.com"), Void.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
