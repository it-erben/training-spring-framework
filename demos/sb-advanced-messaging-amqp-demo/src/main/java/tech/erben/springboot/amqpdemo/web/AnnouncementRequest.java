package tech.erben.springboot.amqpdemo.web;

import jakarta.validation.constraints.NotBlank;

public record AnnouncementRequest(@NotBlank String message) {}
