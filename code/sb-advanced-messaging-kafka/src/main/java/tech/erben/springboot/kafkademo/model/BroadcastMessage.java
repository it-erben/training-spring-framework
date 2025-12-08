package tech.erben.springboot.kafkademo.model;

import java.time.Instant;

public record BroadcastMessage(String message, Instant createdAt) {}
