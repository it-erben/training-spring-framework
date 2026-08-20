package tech.erben.springboot.batch.task;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ParticipantJobConfig {

    @Bean
    public FlatFileItemReader<ParticipantLine> participantReader() {
        // TODO Aufgabe 1: FlatFileItemReaderBuilder verwenden. Namen setzen,
        //  participants-defekt.csv als ClassPathResource laden, Kopfzeile
        //  überspringen, delimited(), Spalten participantId, fullName, email,
        //  courseCode, targetType ParticipantLine.
        throw new UnsupportedOperationException("Aufgabe 1");
    }

    @Bean
    public ItemProcessor<ParticipantLine, ParticipantLine> participantProcessor(
            CourseCatalogClient courseCatalogClient) {
        // TODO Aufgabe 2: E-Mail trimmen und in Kleinbuchstaben wandeln.
        //  Enthält sie kein '@', null zurückgeben (Item wird verworfen und
        //  zählt als filtered). Sonst courseCatalogClient.check(courseCode)
        //  aufrufen und ein neues ParticipantLine mit der normalisierten
        //  E-Mail liefern.
        throw new UnsupportedOperationException("Aufgabe 2");
    }

    @Bean
    public JdbcBatchItemWriter<ParticipantLine> participantWriter(DataSource dataSource) {
        // TODO Aufgabe 3: JdbcBatchItemWriterBuilder verwenden. Insert in
        //  participant (participant_id, full_name, email, course_code) mit
        //  benannten Parametern, beanMapped().
        throw new UnsupportedOperationException("Aufgabe 3");
    }

    @Bean
    public Step participantImportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      FlatFileItemReader<ParticipantLine> participantReader,
                                      ItemProcessor<ParticipantLine, ParticipantLine> participantProcessor,
                                      JdbcBatchItemWriter<ParticipantLine> participantWriter) {
        // TODO Aufgabe 4: StepBuilder("participantImportStep", jobRepository)
        //  mit chunk(3), transactionManager, reader, processor und writer
        //  verdrahten.
        //
        // TODO Aufgabe 6: faultTolerant() ergänzen.
        //  FlatFileParseException überspringen, skipLimit(3).
        //  CatalogUnavailableException wiederholen, retryLimit(3).
        //
        // TODO Aufgabe 7: skipListener(new RejectListener(
        //  Path.of("target/rejected-participants.csv"))) registrieren.
        throw new UnsupportedOperationException("Aufgabe 4");
    }

    @Bean
    public Job participantImportJob(JobRepository jobRepository, Step participantImportStep) {
        // TODO Aufgabe 5: JobBuilder("participantImportJob", jobRepository)
        //  mit start(participantImportStep) verdrahten.
        throw new UnsupportedOperationException("Aufgabe 5");
    }
}
