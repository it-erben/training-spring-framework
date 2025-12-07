package tech.erben.springboot.actuatordemo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final List<Order> orders = new CopyOnWriteArrayList<>();

    public List<Order> getOrders() {
        return orders;
    }

    public Order createOrder(String product) {
        Order order = new Order(UUID.randomUUID().toString(), product, Instant.now());
        orders.add(order);
        return order;
    }
}
