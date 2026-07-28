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
 * Prueft die Actuator-Endpoints gegen die komplette, auf einem
 * zufaelligen Port gestartete Anwendung — genau so, wie spaeter eine
 * Liveness-Probe oder ein Monitoring-System zugreifen wuerde.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ActuatorEndpointsTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("/actuator/health antwortet mit 200, Status UP und Details")
    void healthReturnsUpWithDetails() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        // show-details=always: die einzelnen Indikatoren (z.B. diskSpace)
        // erscheinen im Body, nicht nur der aggregierte Status.
        assertThat(response.getBody()).contains("diskSpace");
    }

    @Test
    @DisplayName("/actuator/info liefert den konfigurierten App-Namen")
    void infoContainsAppName() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Buchhandlung Erben");
    }

    @Test
    @DisplayName("/actuator/metrics ist freigeschaltet und kennt JVM-Metriken")
    void metricsListsJvmMetrics() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/metrics", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("jvm.memory.used");
    }
}
