package tech.erben.springboot.batch.task;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ParticipantJobConfig {

    @Bean
    public FlatFileItemReader<ParticipantLine> participantReader() {
        return new FlatFileItemReaderBuilder<ParticipantLine>()
                .name("participantReader")
                .resource(new ClassPathResource("participants-defekt.csv"))
                .linesToSkip(1)
                .delimited()
                .names("participantId", "fullName", "email", "courseCode")
                .targetType(ParticipantLine.class)
                .build();
    }

    // Rückgabe null verwirft das Item: es erreicht den Writer nie und zählt
    // als filtered, nicht als written.
    @Bean
    public ItemProcessor<ParticipantLine, ParticipantLine> participantProcessor(
            CourseCatalogClient courseCatalogClient) {
        return line -> {
            String email = line.email().trim().toLowerCase();
            if (!email.contains("@")) {
                return null;
            }
            courseCatalogClient.check(line.courseCode());
            return new ParticipantLine(line.participantId(), line.fullName(), email, line.courseCode());
        };
    }

    @Bean
    public JdbcBatchItemWriter<ParticipantLine> participantWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<ParticipantLine>()
                .dataSource(dataSource)
                .sql("insert into participant (participant_id, full_name, email, course_code) "
                        + "values (:participantId, :fullName, :email, :courseCode)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step participantImportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      FlatFileItemReader<ParticipantLine> participantReader,
                                      ItemProcessor<ParticipantLine, ParticipantLine> participantProcessor,
                                      JdbcBatchItemWriter<ParticipantLine> participantWriter) {
        return new StepBuilder("participantImportStep", jobRepository)
                .<ParticipantLine, ParticipantLine>chunk(3)
                .transactionManager(transactionManager)
                .reader(participantReader)
                .processor(participantProcessor)
                .writer(participantWriter)
                .faultTolerant()
                // Datenfehler: eine kaputte Zeile bleibt kaputt, überspringen.
                .skip(FlatFileParseException.class)
                .skipLimit(3)
                .skipListener(new RejectListener(Path.of("target/rejected-participants.csv")))
                // Technischer Fehler: derselbe Aufruf kann später gelingen.
                .retry(CatalogUnavailableException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Job participantImportJob(JobRepository jobRepository, Step participantImportStep) {
        return new JobBuilder("participantImportJob", jobRepository)
                .start(participantImportStep)
                .build();
    }
}
