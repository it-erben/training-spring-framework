package tech.erben.springboot.batch.task;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

// Steht für eine technische Abhängigkeit im Processor, die zeitweise nicht
// antwortet. Der feste Rhythmus macht den Retry reproduzierbar.
@Component
public class CourseCatalogClient {

    private final AtomicInteger calls = new AtomicInteger();

    public void check(String courseCode) {
        if (calls.incrementAndGet() % 4 == 0) {
            throw new CatalogUnavailableException("Kurskatalog antwortet nicht: " + courseCode);
        }
    }
}
