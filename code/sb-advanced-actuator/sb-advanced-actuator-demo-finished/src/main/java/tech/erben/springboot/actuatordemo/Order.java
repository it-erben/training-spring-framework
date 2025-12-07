package tech.erben.springboot.actuatordemo;

import java.time.Instant;

public record Order(String id, String product, Instant createdAt) {}
