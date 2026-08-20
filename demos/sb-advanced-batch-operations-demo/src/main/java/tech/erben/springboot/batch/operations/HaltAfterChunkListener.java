package tech.erben.springboot.batch.operations;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;

/**
 * Erzeugt den Absturz für Szenario 2. {@code halt} statt {@code exit}: Ein
 * {@code exit} liefe durch die Shutdown-Hooks, und Spring Batch bekäme die
 * Gelegenheit, den Status der Execution noch zu korrigieren — genau das, was
 * bei einem echten Absturz ausbleibt. Die Execution muss auf {@code STARTED}
 * stehen bleiben, sonst gibt es nichts aufzuräumen.
 */
public class HaltAfterChunkListener implements ChunkListener<OrderLine, OrderLine> {

    private static final Logger log = LoggerFactory.getLogger(HaltAfterChunkListener.class);

    private final int haltAfterChunk;
    private final AtomicInteger committed = new AtomicInteger();

    public HaltAfterChunkListener(int haltAfterChunk) {
        this.haltAfterChunk = haltAfterChunk;
    }

    @Override
    public void afterChunk(Chunk<OrderLine> chunk) {
        int chunkNumber = committed.incrementAndGet();
        log.info("Chunk {} committed, {} Items", chunkNumber, chunk.size());
        if (haltAfterChunk > 0 && chunkNumber >= haltAfterChunk) {
            log.error("Simulierter Absturz nach Chunk {}", chunkNumber);
            Runtime.getRuntime().halt(1);
        }
    }
}
