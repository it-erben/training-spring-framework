package tech.erben.springboot.amqpdemo.model;

import java.time.Instant;

public record ProcessedMessage(
    String queue,
    String note,
    Object payload,
    Instant processedAt
) {}
