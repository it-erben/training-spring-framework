package tech.erben.springboot.kafkademo.model;

import java.time.Instant;

public record ProcessedMessage(
    String topic,
    String note,
    Object payload,
    Instant processedAt
) {}
