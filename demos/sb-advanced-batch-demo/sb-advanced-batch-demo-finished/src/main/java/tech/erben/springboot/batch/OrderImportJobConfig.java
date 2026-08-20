package tech.erben.springboot.batch;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OrderImportJobConfig {

    @Bean
    public FlatFileItemReader<OrderLine> orderReader(@Value("${demo.batch.input}") String input) {
        return new FlatFileItemReaderBuilder<OrderLine>()
                .name("orderReader")
                .resource(new ClassPathResource(input))
                .linesToSkip(1)
                .delimited()
                .names("orderId", "title", "quantity", "unitPriceCents")
                .targetType(OrderLine.class)
                .build();
    }

    // Zwei Aufgaben in einem Schritt: filtern und anreichern. Rückgabe null
    // verwirft das Item, es erreicht den Writer nie und zählt als filtered.
    @Bean
    public ItemProcessor<OrderLine, OrderLine> pricingProcessor(PricingClient pricingClient) {
        return line -> line.quantity() <= 0 ? null : pricingClient.enrich(line);
    }

    @Bean
    public JdbcBatchItemWriter<OrderLine> orderWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<OrderLine>()
                .dataSource(dataSource)
                .sql("insert into book_order (order_id, title, quantity, unit_price_cents) "
                        + "values (:orderId, :title, :quantity, :unitPriceCents)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<OrderLine> orderReader,
                           ItemProcessor<OrderLine, OrderLine> pricingProcessor,
                           JdbcBatchItemWriter<OrderLine> orderWriter,
                           @Value("${demo.batch.skip-limit}") int skipLimit) {
        return new StepBuilder("importStep", jobRepository)
                .<OrderLine, OrderLine>chunk(2)
                .transactionManager(transactionManager)
                .reader(orderReader)
                .processor(pricingProcessor)
                .writer(orderWriter)
                .listener(new ChunkBoundaryLogger())
                .faultTolerant()
                // Datenfehler: eine kaputte Zeile bleibt kaputt, überspringen.
                .skip(FlatFileParseException.class)
                .skipLimit(skipLimit)
                .skipListener(new RejectFileSkipListener(Path.of("target/rejected.csv")))
                // Technischer Fehler: derselbe Aufruf kann später gelingen.
                .retry(PricingUnavailableException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public JdbcCursorItemReader<OrderLine> reportReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<OrderLine>()
                .name("reportReader")
                .dataSource(dataSource)
                .sql("select order_id, title, quantity, unit_price_cents from book_order order by order_id")
                .rowMapper((rs, rowNum) -> new OrderLine(
                        rs.getString("order_id"),
                        rs.getString("title"),
                        rs.getInt("quantity"),
                        rs.getInt("unit_price_cents")))
                .build();
    }

    @Bean
    public FlatFileItemWriter<OrderLine> reportWriter(@Value("${demo.batch.report}") String report) {
        // fieldExtractor statt sourceType()+names(): totalCents() ist keine
        // Record-Komponente, sondern ein abgeleiteter Wert. Der
        // RecordFieldExtractor hinter sourceType() akzeptiert ausschließlich
        // tatsächliche Record-Komponenten und lehnt "totalCents" beim
        // Bean-Start mit IllegalArgumentException ab.
        return new FlatFileItemWriterBuilder<OrderLine>()
                .name("reportWriter")
                .resource(new FileSystemResource(report))
                .headerCallback(writer -> writer.write("orderId;title;totalCents"))
                .delimited()
                .delimiter(";")
                .fieldExtractor(order -> new Object[] {order.orderId(), order.title(), order.totalCents()})
                .build();
    }

    @Bean
    public Step reportStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           JdbcCursorItemReader<OrderLine> reportReader,
                           FlatFileItemWriter<OrderLine> reportWriter) {
        return new StepBuilder("reportStep", jobRepository)
                .<OrderLine, OrderLine>chunk(10)
                .transactionManager(transactionManager)
                .reader(reportReader)
                .writer(reportWriter)
                .build();
    }

    @Bean
    public Step archiveStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            @Value("${demo.batch.report}") String report,
                            @Value("${demo.batch.archive-dir}") String archiveDir) {
        return new StepBuilder("archiveStep", jobRepository)
                .tasklet(new ArchiveReportTasklet(Path.of(report), Path.of(archiveDir)), transactionManager)
                .build();
    }

    @Bean
    public Job orderImportJob(JobRepository jobRepository, Step importStep, Step reportStep, Step archiveStep) {
        return new JobBuilder("orderImportJob", jobRepository)
                .start(importStep)
                .next(reportStep)
                .next(archiveStep)
                .build();
    }
}
