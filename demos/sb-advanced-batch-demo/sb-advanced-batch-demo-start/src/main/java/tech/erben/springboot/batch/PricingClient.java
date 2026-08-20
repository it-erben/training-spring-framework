package tech.erben.springboot.batch;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Steht für eine technische Abhängigkeit im Processor, die zeitweise nicht
// antwortet. Der Ausfall ist zuschaltbar, damit der erste Lauf sauber
// durchläuft und der Fehlerfall danach gezielt vorgeführt werden kann; der
// feste Rhythmus macht ihn reproduzierbar.
@Component
public class PricingClient {

    private final boolean flaky;

    private final AtomicInteger calls = new AtomicInteger();

    public PricingClient(@Value("${demo.batch.pricing.flaky}") boolean flaky) {
        this.flaky = flaky;
    }

    public OrderLine enrich(OrderLine line) {
        if (flaky && calls.incrementAndGet() % 3 == 0) {
            throw new PricingUnavailableException("Preisdienst antwortet nicht");
        }
        return line;
    }
}
