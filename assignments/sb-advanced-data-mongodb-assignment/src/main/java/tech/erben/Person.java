package tech.erben;

import org.springframework.data.annotation.Id;

import java.time.LocalDate;

public record Person(
    @Id String id,
    String firstname,
    String lastname,
    LocalDate birthday
) {}
