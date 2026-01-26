package tech.erben.springboot.tracing.api;

import tech.erben.springboot.tracing.model.Customer;
import tech.erben.springboot.tracing.model.CustomerOrder;

import java.math.BigDecimal;
import java.util.List;

public record CustomerResponse(
        long id,
        String name,
        String email,
        List<OrderResponse> orders,
        BigDecimal totalAmount
) {
    public static CustomerResponse from(Customer customer, List<CustomerOrder> orders) {
        List<OrderResponse> orderResponses = orders.stream()
                .map(OrderResponse::from)
                .toList();
        BigDecimal total = orders.stream()
                .map(CustomerOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                orderResponses,
                total
        );
    }
}
