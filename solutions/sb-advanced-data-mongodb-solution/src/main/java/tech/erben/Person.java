package tech.erben;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.annotation.Id;

public record Person(
    @Id String id,
    String firstname,
    String lastname,
    LocalDate birthday,
    List<PhoneNumber> phonenumbers
) {}
