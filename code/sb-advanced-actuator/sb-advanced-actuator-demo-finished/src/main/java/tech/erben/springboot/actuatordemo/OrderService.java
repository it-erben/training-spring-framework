package tech.erben.springboot.actuatordemo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final List<Order> orders = new CopyOnWriteArrayList<>();
    private final Counter orderProcessedCounter;
    private final Timer orderProcessingTimer;
    private final Random random = new Random();

    public OrderService(MeterRegistry meterRegistry) {
        this.orderProcessedCounter =
            meterRegistry.counter("orders.processed", "status", "success");
        this.orderProcessingTimer = meterRegistry.timer("orders.processing.duration");
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Order createOrder(String product) {
        return orderProcessingTimer.record(() -> {
            simulateLatency();
            Order order = new Order(UUID.randomUUID().toString(), product, Instant.now());
            orders.add(order);
            orderProcessedCounter.increment();
            log.info("Order created for {}", product);
            return order;
        });
    }

    private void simulateLatency() {
        try {
            Thread.sleep(random.nextInt(50) + 10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
