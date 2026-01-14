package tech.erben.springboot.resilience4jdemo.model;

import java.math.BigDecimal;

public record Product(
        String id,
        String name,
        BigDecimal price,
        String priceSource
) {
    public Product withPrice(BigDecimal newPrice, String source) {
        return new Product(id, name, newPrice, source);
    }
}
