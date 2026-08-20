package tech.erben.springboot.batch;

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

// spring.datasource.url: die Anwendung nutzt standardmäßig eine dateibasierte
// H2-Instanz (siehe application.yml), damit ein JVM-Neustart in der Demo den
// JobRepository-Zustand behält. Ohne diese Überschreibung träfe der Testlauf
// auf eine bereits abgeschlossene JobInstance aus einem früheren Lauf.
@SpringBatchTest
@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:import-test;DB_CLOSE_DELAY=-1",
        "demo.batch.input=orders-defekt.csv",
        "demo.batch.pricing.flaky=true"
})
class OrderImportJobTest {

    // RejectFileSkipListener hängt an einen festen Pfad an (CREATE, APPEND) —
    // ohne Löschen vor dem Lauf sammeln sich Zeilen aus früheren Testläufen an.
    private static final Path REJECTED_FILE = Path.of("target/rejected.csv");

    private static final Path ARCHIVED_REPORT = Path.of("target/archive/revenue-report.csv");

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @BeforeEach
    void loescheErgebnisdateien() throws Exception {
        Files.deleteIfExists(REJECTED_FILE);
        Files.deleteIfExists(ARCHIVED_REPORT);
    }

    // Ein einziger Lauf trägt alle Assertions: PricingClient zählt seine
    // Aufrufe über die Lebensdauer des Kontexts, und book_order behält seine
    // Zeilen. Ein zweiter Lauf im selben Kontext träfe deshalb auf einen
    // anderen Fehlerrhythmus und auf den Primärschlüssel bereits
    // geschriebener Bestellungen.
    @Test
    void ueberspringtDefekteZeilenFaengtDenDienstausfallAbUndBleibtErfolgreich() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        StepExecution step = execution.getStepExecutions().stream()
                .filter(each -> each.getStepName().equals("importStep"))
                .findFirst()
                .orElseThrow();
        assertThat(step.getSkipCount()).isEqualTo(2);
        assertThat(step.getFilterCount()).isEqualTo(1);
        assertThat(step.getWriteCount()).isEqualTo(3);

        assertThat(Files.readAllLines(REJECTED_FILE)).containsExactly(
                "Parsing error at line: 3 in resource=[class path resource [orders-defekt.csv]], "
                        + "input=[O-2,Kaputt]",
                "Parsing error at line: 5 in resource=[class path resource [orders-defekt.csv]], "
                        + "input=[O-4,Domain-Driven Design,drei,5990]");

        // Der Tasklet-Step hat den Bericht verschoben, nicht kopiert.
        assertThat(ARCHIVED_REPORT).exists();
        assertThat(Path.of("target/revenue-report.csv")).doesNotExist();
    }
}
