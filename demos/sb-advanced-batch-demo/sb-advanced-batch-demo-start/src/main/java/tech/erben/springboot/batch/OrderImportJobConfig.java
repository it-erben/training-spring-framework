package tech.erben.springboot.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OrderImportJobConfig {

    @Bean
    public FlatFileItemReader<OrderLine> orderReader(@Value("${demo.batch.input}") String input) {
        // TODO Schritt 1: FlatFileItemReaderBuilder verwenden. Namen setzen,
        //  input als ClassPathResource laden, Kopfzeile überspringen,
        //  delimited(), Spalten orderId, title, quantity, unitPriceCents,
        //  targetType OrderLine.
        throw new UnsupportedOperationException("Schritt 1");
    }

    @Bean
    public ItemProcessor<OrderLine, OrderLine> pricingProcessor(PricingClient pricingClient) {
        // TODO Schritt 2: Items mit quantity() <= 0 mit null verwerfen, sonst
        //  pricingClient.enrich(line) zurückgeben.
        throw new UnsupportedOperationException("Schritt 2");
    }

    @Bean
    public JdbcBatchItemWriter<OrderLine> orderWriter(DataSource dataSource) {
        // TODO Schritt 3: JdbcBatchItemWriterBuilder verwenden. Insert in
        //  book_order (order_id, title, quantity, unit_price_cents) mit
        //  benannten Parametern, beanMapped().
        throw new UnsupportedOperationException("Schritt 3");
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<OrderLine> orderReader,
                           ItemProcessor<OrderLine, OrderLine> pricingProcessor,
                           JdbcBatchItemWriter<OrderLine> orderWriter) {
        // TODO Schritt 4: StepBuilder("importStep", jobRepository) mit
        //  chunk(2), transactionManager, reader, processor, writer und dem
        //  ChunkBoundaryLogger als Listener.
        throw new UnsupportedOperationException("Schritt 4");
    }

    @Bean
    public Job orderImportJob(JobRepository jobRepository, Step importStep) {
        // TODO Schritt 5: JobBuilder("orderImportJob", jobRepository) mit
        //  start(importStep).
        throw new UnsupportedOperationException("Schritt 5");
    }
}
