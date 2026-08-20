package tech.erben.springboot.batch.task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.batch.core.listener.SkipListener;

// Ausschüsse gehören in eine Datei, nicht nur ins Log: der Fachbereich muss
// nachvollziehen können, welche Anmeldungen nicht angekommen sind.
public class RejectListener implements SkipListener<ParticipantLine, ParticipantLine> {

    private final Path target;

    public RejectListener(Path target) {
        this.target = target;
    }

    @Override
    public void onSkipInRead(Throwable cause) {
        append(cause.getMessage());
    }

    @Override
    public void onSkipInProcess(ParticipantLine item, Throwable cause) {
        append(item.participantId() + ";" + cause.getMessage());
    }

    private void append(String line) {
        try {
            Files.writeString(target, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Ausschussdatei nicht schreibbar", ex);
        }
    }
}
