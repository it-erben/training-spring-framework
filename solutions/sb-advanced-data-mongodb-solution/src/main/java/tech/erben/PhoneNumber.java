package tech.erben;

import org.springframework.data.annotation.Id;

public record PhoneNumber(@Id String id, Integer countryCode, Integer number) {}
