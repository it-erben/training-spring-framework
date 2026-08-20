package tech.erben.springboot.batch.operations;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Schalter der Vorführung. Jedes Störfall-Szenario des Moduls ist über diese
 * Werte reproduzierbar, keines hängt an Timing von Hand.
 *
 * @param writer         {@code insert} schreibt stur ein, {@code upsert} führt zusammen
 * @param haltAfterChunk größer als 0 tötet die JVM nach diesem Chunk
 * @param lock           schaltet die Sperrtabelle vor dem Start scharf
 * @param input          eingelesene CSV-Datei aus dem Klassenpfad
 * @param inputB         zweite Datei, die der parallele Step liest
 * @param parallel       schaltet die beiden Import-Steps von nacheinander auf gleichzeitig
 * @param itemDelay      Wartezeit je Item; hält den Lauf lang genug für Beobachtung
 */
@ConfigurationProperties("ops")
public record OperationsProperties(String writer,
                                   int haltAfterChunk,
                                   boolean lock,
                                   String input,
                                   String inputB,
                                   boolean parallel,
                                   Duration itemDelay) {
}
