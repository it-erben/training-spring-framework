package tech.erben.springboot.batch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.batch.core.listener.SkipListener;

// Ausschüsse gehören in eine Datei, nicht nur ins Log: der Fachbereich muss
// nachvollziehen können, welche Zeilen nicht angekommen sind.
public class RejectFileSkipListener implements SkipListener<OrderLine, OrderLine> {

    private final Path target;

    public RejectFileSkipListener(Path target) {
        this.target = target;
    }

    @Override
    public void onSkipInRead(Throwable cause) {
        append(cause.getMessage());
    }

    @Override
    public void onSkipInProcess(OrderLine item, Throwable cause) {
        append(item.orderId() + ";" + cause.getMessage());
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
