package tech.erben.springboot.kafkademo.web;

import jakarta.validation.constraints.NotBlank;

public record AnnouncementRequest(@NotBlank String message) {}
