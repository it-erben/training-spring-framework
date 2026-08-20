package tech.erben.springboot.batch.operations;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OrderImportJobConfig {

    public static final String JOB_NAME = "orderImportJob";

    private static final String INSERT_SQL =
            "insert into book_order (order_id, title, quantity, unit_price_cents) "
                    + "values (:orderId, :title, :quantity, :unitPriceCents)";

    // MERGE trifft die Entscheidung anhand des Primärschlüssels. Damit wird
    // aus demselben Job ein wiederholbarer Lauf, ohne dass sich an Reader,
    // Step oder Parametern etwas ändert.
    private static final String UPSERT_SQL =
            "merge into book_order (order_id, title, quantity, unit_price_cents) "
                    + "key (order_id) values (:orderId, :title, :quantity, :unitPriceCents)";

    /**
     * Der Job wird über HTTP gestartet und soll nebenläufig laufen. Ohne
     * diesen TaskExecutor liefe er im Request-Thread: Der Aufruf käme erst
     * nach dem letzten Chunk zurück, und zwei gleichzeitige Starts wären nicht
     * vorführbar. {@code @BatchTaskExecutor} reicht ihn an den vom Starter
     * gebauten {@code JobOperator} durch.
     */
    @Bean
    @BatchTaskExecutor
    public TaskExecutor batchTaskExecutor() {
        return new SimpleAsyncTaskExecutor("job-");
    }

    @Bean
    public FlatFileItemReader<OrderLine> orderReader(OperationsProperties properties) {
        return csvReader("orderReader", properties.input());
    }

    @Bean
    public FlatFileItemReader<OrderLine> orderReaderB(OperationsProperties properties) {
        return csvReader("orderReaderB", properties.inputB());
    }

    private FlatFileItemReader<OrderLine> csvReader(String name, String resource) {
        return new FlatFileItemReaderBuilder<OrderLine>()
                .name(name)
                .resource(new ClassPathResource(resource))
                .linesToSkip(1)
                .delimited()
                .names("orderId", "title", "quantity", "unitPriceCents")
                .targetType(OrderLine.class)
                .build();
    }

    /**
     * Eigener Executor für den Split. Der {@code @BatchTaskExecutor} von oben
     * startet ganze Jobs; dieser hier verteilt die Flows eines Jobs. Beide zu
     * mischen macht die Auslastung unübersichtlich, sobald mehrere Jobs
     * gleichzeitig laufen.
     */
    @Bean
    public TaskExecutor stepTaskExecutor() {
        return new SimpleAsyncTaskExecutor("step-");
    }

    /**
     * Steht für einen langsamen Fremdaufruf. Ohne die Wartezeit wäre der Lauf
     * nach Millisekunden vorbei, und weder laufende Executions noch ein
     * Doppelstart ließen sich beobachten.
     */
    @Bean
    public ItemProcessor<OrderLine, OrderLine> slowProcessor(OperationsProperties properties) {
        return line -> {
            Thread.sleep(properties.itemDelay());
            return line;
        };
    }

    @Bean
    public JdbcBatchItemWriter<OrderLine> orderWriter(DataSource dataSource, OperationsProperties properties) {
        return new JdbcBatchItemWriterBuilder<OrderLine>()
                .dataSource(dataSource)
                .sql("upsert".equals(properties.writer()) ? UPSERT_SQL : INSERT_SQL)
                .beanMapped()
                .build();
    }

    @Bean
    public Step importStepB(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            FlatFileItemReader<OrderLine> orderReaderB,
                            ItemProcessor<OrderLine, OrderLine> slowProcessor,
                            JdbcBatchItemWriter<OrderLine> orderWriter) {
        return new StepBuilder("importStepB", jobRepository)
                .<OrderLine, OrderLine>chunk(2)
                .transactionManager(transactionManager)
                .reader(orderReaderB)
                .processor(slowProcessor)
                .writer(orderWriter)
                .build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<OrderLine> orderReader,
                           ItemProcessor<OrderLine, OrderLine> slowProcessor,
                           JdbcBatchItemWriter<OrderLine> orderWriter,
                           OperationsProperties properties) {
        return new StepBuilder("importStep", jobRepository)
                .<OrderLine, OrderLine>chunk(2)
                .transactionManager(transactionManager)
                .reader(orderReader)
                .processor(slowProcessor)
                .writer(orderWriter)
                .listener(new HaltAfterChunkListener(properties.haltAfterChunk()))
                .build();
    }

    /**
     * Zwei Importe, die einander nicht brauchen: Der Split lässt sie
     * gleichzeitig laufen, die Reihenfolge nacheinander. Beide schreiben in
     * dieselbe Tabelle, aber auf getrennte Schlüsselbereiche — ohne diese
     * Trennung wäre die Parallelität ein Datenrennen.
     */
    @Bean
    public Job orderImportJob(JobRepository jobRepository,
                              Step importStep,
                              Step importStepB,
                              TaskExecutor stepTaskExecutor,
                              JobLockRepository jobLockRepository,
                              OperationsProperties properties) {
        if (!properties.parallel()) {
            return new JobBuilder(JOB_NAME, jobRepository)
                    .start(importStep)
                    .next(importStepB)
                    .listener(new JobLockListener(jobLockRepository))
                    .build();
        }
        Flow flowA = new FlowBuilder<SimpleFlow>("flowA").start(importStep).build();
        Flow flowB = new FlowBuilder<SimpleFlow>("flowB").start(importStepB).build();
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(flowA)
                .split(stepTaskExecutor)
                .add(flowB)
                .end()
                .listener(new JobLockListener(jobLockRepository))
                .build();
    }
}
