package tech.erben.springboot.kafkademo.model;

import java.time.Instant;

public record OrderMessage(
    String id,
    String customer,
    String item,
    int quantity,
    boolean priority,
    boolean simulateError,
    String targetTopic,
    Instant createdAt
) {}
