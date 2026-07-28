package tech.erben.springboot.basics.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End-Test gegen den echten Web-Stack: {@code RANDOM_PORT} startet
 * den eingebetteten Tomcat, den die AutoConfiguration allein aus der
 * Abhaengigkeit {@code spring-boot-starter-web} aufsetzt — genau das
 * belegt dieser Test nebenbei. Ohne aktives Profil muss {@code GET /info}
 * die unveraenderten Basis-Properties und eine leere Profil-Liste liefern.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("GET /info liefert die gebundenen Properties und die aktiven Profile")
class ShopInfoControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void infoReturnsBoundPropertiesAndActiveProfiles() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/info", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MediaType contentType = response.getHeaders().getContentType();
        assertThat(contentType).isNotNull();
        assertThat(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getBody())
                .containsEntry("name", "Buchhandlung Erben")
                .containsEntry("currency", "EUR")
                .containsEntry("pageSize", 20)
                .containsEntry("activeProfiles", List.of());
    }
}
