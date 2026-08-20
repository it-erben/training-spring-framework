package tech.erben.springboot.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

// Ein einmaliger Handgriff ohne Items: kein Reader, kein Writer, keine
// Chunk-Größe. Genau dafür gibt es Tasklets.
public class ArchiveReportTasklet implements Tasklet {

    private final Path report;

    private final Path archiveDir;

    public ArchiveReportTasklet(Path report, Path archiveDir) {
        this.report = report;
        this.archiveDir = archiveDir;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (!Files.exists(report)) {
            // Der Exit-Status ist der Rückkanal des Tasklets an den Job: ein
            // nachgelagerter Flow kann darauf verzweigen, ohne die Datei selbst
            // zu kennen.
            contribution.setExitStatus(new ExitStatus("NO_REPORT"));
            return RepeatStatus.FINISHED;
        }
        Files.createDirectories(archiveDir);
        Files.move(report, archiveDir.resolve(report.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        return RepeatStatus.FINISHED;
    }
}
