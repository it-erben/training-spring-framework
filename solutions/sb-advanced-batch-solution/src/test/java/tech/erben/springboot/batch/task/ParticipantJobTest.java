package tech.erben.springboot.batch.task;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Dieser Test braucht den ganzen Kontext und damit alle fünf Beans. Die
// Aufgaben 1 bis 3 prüft ParticipantBeansTest einzeln und ohne Kontext.
@SpringBatchTest
@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:participant-test;DB_CLOSE_DELAY=-1"
})
class ParticipantJobTest {

    // RejectListener hängt an einen festen Pfad an (CREATE, APPEND) — ohne
    // Löschen vor dem Lauf sammeln sich Zeilen aus früheren Läufen an.
    private static final Path REJECTED_FILE = Path.of("target/rejected-participants.csv");

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @BeforeEach
    void loescheAusschussdatei() throws Exception {
        Files.deleteIfExists(REJECTED_FILE);
    }

    // Ein einziger Lauf trägt alle Assertions: CourseCatalogClient zählt seine
    // Aufrufe über die Lebensdauer des Kontexts, und participant behält seine
    // Zeilen. Ein zweiter Lauf im selben Kontext träfe deshalb auf einen
    // anderen Fehlerrhythmus und auf den Primärschlüssel bereits
    // geschriebener Anmeldungen.
    @Test
    void aufgabe4Bis7_jobLaeuftFehlertolerantDurch() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        StepExecution step = execution.getStepExecutions().stream()
                .filter(each -> each.getStepName().equals("participantImportStep"))
                .findFirst()
                .orElseThrow();
        assertThat(step.getSkipCount()).isEqualTo(2);
        assertThat(step.getFilterCount()).isEqualTo(1);
        assertThat(step.getWriteCount()).isEqualTo(5);

        assertThat(Files.readAllLines(REJECTED_FILE)).hasSize(2);
    }
}
