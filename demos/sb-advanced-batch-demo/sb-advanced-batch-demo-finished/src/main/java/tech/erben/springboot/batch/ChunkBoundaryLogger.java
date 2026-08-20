package tech.erben.springboot.batch;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;

// Der Listener bekommt je Aufruf nur die Items des gerade committeten Chunks,
// keine Zählerstände des Steps. Eine laufende Summe muss er deshalb selbst
// führen, um die Chunk-Grenze im Log sichtbar zu machen.
public class ChunkBoundaryLogger implements ChunkListener<OrderLine, OrderLine> {

    private static final Logger log = LoggerFactory.getLogger(ChunkBoundaryLogger.class);

    private final AtomicInteger written = new AtomicInteger();

    @Override
    public void afterChunk(Chunk<OrderLine> chunk) {
        log.info("Chunk committed, bisher geschrieben: {}", written.addAndGet(chunk.size()));
    }
}
