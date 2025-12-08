package tech.erben.springboot.amqpdemo.model;

import java.time.Instant;

public record OrderMessage(
    String id,
    String customer,
    String item,
    int quantity,
    boolean priority,
    boolean simulateError,
    String routingKey,
    Instant createdAt
) {}
