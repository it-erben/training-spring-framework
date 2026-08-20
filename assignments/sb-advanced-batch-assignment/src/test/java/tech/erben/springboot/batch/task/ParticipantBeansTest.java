package tech.erben.springboot.batch.task;

import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.assertj.core.api.Assertions.assertThat;

// Ohne Spring-Kontext: Die Beans werden direkt aus der Konfigurationsklasse
// geholt. Dadurch gibt jede der Aufgaben 1 bis 3 für sich Rückmeldung, statt
// erst dann, wenn auch Step und Job stehen und der Kontext hochkommt.
class ParticipantBeansTest {

    private final ParticipantJobConfig config = new ParticipantJobConfig();

    @Test
    void aufgabe1_readerLiestDieErsteAnmeldung() throws Exception {
        FlatFileItemReader<ParticipantLine> reader = config.participantReader();
        reader.open(new ExecutionContext());
        try {
            ParticipantLine first = reader.read();

            assertThat(first).isNotNull();
            assertThat(first.participantId()).isEqualTo("P-1");
            assertThat(first.fullName()).isEqualTo("Anna Behrens");
            assertThat(first.courseCode()).isEqualTo("SB-BASIS");
        }
        finally {
            reader.close();
        }
    }

    @Test
    void aufgabe2_processorNormalisiertEmailUndVerwirftUngueltige() throws Exception {
        ItemProcessor<ParticipantLine, ParticipantLine> processor =
                config.participantProcessor(new CourseCatalogClient());

        ParticipantLine normalisiert = processor.process(
                new ParticipantLine("P-1", "Anna Behrens", "  Anna.Behrens@Example.COM ", "SB-BASIS"));

        assertThat(normalisiert).isNotNull();
        assertThat(normalisiert.email()).isEqualTo("anna.behrens@example.com");
        assertThat(normalisiert.participantId()).isEqualTo("P-1");

        ParticipantLine verworfen = processor.process(
                new ParticipantLine("P-3", "Clara Dorn", "clara.dorn-example.com", "SB-ADV"));

        assertThat(verworfen).isNull();
    }

    @Test
    void aufgabe3_writerSchreibtNachParticipant() throws Exception {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("schema.sql")
                .build();
        try {
            JdbcBatchItemWriter<ParticipantLine> writer = config.participantWriter(database);
            writer.afterPropertiesSet();

            writer.write(Chunk.of(
                    new ParticipantLine("P-99", "Test Teilnehmerin", "test@example.com", "SB-ADV")));

            assertThat(new JdbcTemplate(database).queryForObject(
                    "select full_name from participant where participant_id = 'P-99'", String.class))
                    .isEqualTo("Test Teilnehmerin");
        }
        finally {
            database.shutdown();
        }
    }
}
