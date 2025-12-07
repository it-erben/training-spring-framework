package tech.erben.springboot.actuator;

import java.time.Instant;

public record Order(String id, String product, Instant createdAt) {}
