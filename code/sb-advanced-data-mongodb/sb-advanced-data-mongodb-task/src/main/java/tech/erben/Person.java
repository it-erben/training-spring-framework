package tech.erben;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;

public record Person(
    @Id String id,
    String firstname,
    String lastname,
    LocalDate birthday
) {}
