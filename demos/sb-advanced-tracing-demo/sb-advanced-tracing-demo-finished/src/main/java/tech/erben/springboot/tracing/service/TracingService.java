package tech.erben.springboot.tracing.service;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.erben.springboot.tracing.api.CreateOrderRequest;
import tech.erben.springboot.tracing.api.CustomerResponse;
import tech.erben.springboot.tracing.api.OrderResponse;
import tech.erben.springboot.tracing.model.Customer;
import tech.erben.springboot.tracing.model.CustomerOrder;
import tech.erben.springboot.tracing.repository.CustomerOrderRepository;
import tech.erben.springboot.tracing.repository.CustomerRepository;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Service
public class TracingService {

    private static final Duration RECOMMENDATION_DELAY = Duration.ofMillis(120);
    private static final Duration FRAUD_CHECK_DELAY = Duration.ofMillis(80);

    private final CustomerRepository customerRepository;
    private final CustomerOrderRepository orderRepository;
    private final ObservationRegistry observationRegistry;

    public TracingService(
            CustomerRepository customerRepository,
            CustomerOrderRepository orderRepository,
            ObservationRegistry observationRegistry
    ) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.observationRegistry = observationRegistry;
    }

    @Transactional(readOnly = true)
    public CustomerResponse loadCustomer(long customerId, boolean withRecommendations) {
        Customer customer = observe("db.customer.find", () -> customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId))
        );

        List<CustomerOrder> orders = observe(
                "db.order.by-customer",
                () -> orderRepository.findByCustomerId(customer.getId())
        );

        if (withRecommendations) {
            observe("external.recommendations", () -> simulateLatency(RECOMMENDATION_DELAY));
        }

        return CustomerResponse.from(customer, orders);
    }

    @Transactional
    public OrderResponse createOrder(long customerId, CreateOrderRequest request) {
        Customer customer = observe("db.customer.find", () -> customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId))
        );

        observe("external.fraud-check", () -> simulateLatency(FRAUD_CHECK_DELAY));

        CustomerOrder order = new CustomerOrder(request.description(), request.amount());
        order.setCustomer(customer);

        CustomerOrder saved = observe("db.order.save", () -> orderRepository.save(order));
        return OrderResponse.from(saved);
    }

    private void simulateLatency(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void observe(String name, Runnable runnable) {
        Observation.createNotStarted(name, observationRegistry)
                .contextualName(name)
                .observe(runnable);
    }

    private <T> T observe(String name, Supplier<T> supplier) {
        return Observation.createNotStarted(name, observationRegistry)
                .contextualName(name)
                .observe(supplier);
    }
}
