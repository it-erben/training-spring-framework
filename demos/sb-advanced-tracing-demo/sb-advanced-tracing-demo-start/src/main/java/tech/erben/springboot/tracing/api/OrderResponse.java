package tech.erben.springboot.tracing.api;

import tech.erben.springboot.tracing.model.CustomerOrder;

import java.math.BigDecimal;

public record OrderResponse(
        long id,
        String description,
        BigDecimal amount
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getDescription(),
                order.getAmount()
        );
    }
}
