package tech.erben;

import org.springframework.data.annotation.Id;

public record Person(@Id String id, String firstname, String lastname) {
    Person withLastname(String lastname) {
        return new Person(id, firstname, lastname);
    }
}
