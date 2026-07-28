package tech.erben.springboot.basics.operations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Der fachliche Endpoint der Demo: eine kleine Bestelluebersicht der
 * Buchhandlung, an der sich das Logging beobachten laesst.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrderApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/orders liefert 200 und drei Beispielbestellungen")
    void ordersReturnsThreeSampleOrders() {
        ResponseEntity<OrderService.Order[]> response =
                restTemplate.getForEntity("/api/orders", OrderService.Order[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()[0].bookTitle()).isEqualTo("Clean Code");
    }
}
