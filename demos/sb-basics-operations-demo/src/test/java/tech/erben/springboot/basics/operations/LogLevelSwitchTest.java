package tech.erben.springboot.basics.operations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Die Demo-Pointe als Test: Das Log-Level fuer {@code tech.erben} wird
 * zur Laufzeit ueber {@code POST /actuator/loggers/tech.erben} von
 * {@code INFO} auf {@code DEBUG} umgestellt — die {@code debug}-Zeilen
 * aus dem {@link OrderService} erscheinen erst nach dem Umschalten in
 * der Konsole, vorher nicht. {@link OutputCaptureExtension} schneidet
 * die Konsolenausgabe mit, damit der Test das tatsaechlich beweist.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ExtendWith(OutputCaptureExtension.class)
class LogLevelSwitchTest {

    private static final String DEBUG_LINE = "Stelle Beispielbestellungen zusammen";

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void resetLogLevel() {
        // Logger-Level sind JVM-weiter Zustand — zuruecksetzen, damit
        // andere Tests nicht ploetzlich mit DEBUG laufen.
        postLogLevel("INFO");
    }

    @Test
    @DisplayName("Debug-Zeilen erscheinen erst nach dem Umschalten auf DEBUG")
    void debugLinesAppearOnlyAfterRuntimeSwitch(CapturedOutput output) {
        restTemplate.getForEntity("/api/orders", String.class);

        String beforeSwitch = output.getOut();
        assertThat(beforeSwitch).contains("Bestelluebersicht angefragt");
        assertThat(beforeSwitch).doesNotContain(DEBUG_LINE);

        ResponseEntity<Void> switched = postLogLevel("DEBUG");
        assertThat(switched.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        restTemplate.getForEntity("/api/orders", String.class);

        String afterSwitch = output.getOut().substring(beforeSwitch.length());
        assertThat(afterSwitch).contains(DEBUG_LINE);
        assertThat(afterSwitch).contains("Liefere 3 Bestellungen aus");
    }

    private ResponseEntity<Void> postLogLevel(String level) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity("/actuator/loggers/tech.erben",
                new HttpEntity<>(Map.of("configuredLevel", level), headers), Void.class);
    }
}
