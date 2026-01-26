package tech.erben.springboot.tracing.api;

import java.math.BigDecimal;

public record CreateOrderRequest(
        String description,
        BigDecimal amount
) {
}
