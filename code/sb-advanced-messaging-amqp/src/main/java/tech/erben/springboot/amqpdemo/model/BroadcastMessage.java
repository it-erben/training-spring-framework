package tech.erben.springboot.amqpdemo.model;

import java.time.Instant;

public record BroadcastMessage(String message, Instant createdAt) {}
