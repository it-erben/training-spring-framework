package tech.erben.springboot.batch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Eigener Kontext mit eigener Datenbank: skip-limit unterscheidet sich von
// OrderImportJobTest, und book_order darf keine Zeilen aus dessen Lauf sehen.
@SpringBatchTest
@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:skiplimit-test;DB_CLOSE_DELAY=-1",
        "demo.batch.input=orders-defekt.csv",
        "demo.batch.pricing.flaky=true",
        "demo.batch.skip-limit=1"
})
class SkipLimitExceededTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    // Der zweite Skip-Versuch überschreitet das Limit. Der zu diesem Zeitpunkt
    // offene Chunk wird zurückgerollt, der Step bricht ab — der Unterschied
    // zwischen unauffällig und abgebrochen liegt an dieser einen Zahl.
    @Test
    void brichtAbSobaldDasSkipLimitUeberschrittenIst() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);

        StepExecution step = execution.getStepExecutions().stream()
                .filter(each -> each.getStepName().equals("importStep"))
                .findFirst()
                .orElseThrow();
        assertThat(step.getSkipCount()).isEqualTo(1);
        assertThat(step.getFilterCount()).isEqualTo(0);
        assertThat(step.getWriteCount()).isEqualTo(1);
        assertThat(step.getRollbackCount()).isEqualTo(1);
    }
}
