package tech.erben.reactive;


import net.datafaker.Faker;

import java.util.Locale;

public class PersonGenerator {

    public static Person randomPerson() {
        var faker = new Faker(Locale.GERMAN);
        Person person = new Person();
        person.setFirstName(faker.name().firstName());
        person.setLastName(faker.name().lastName());
        return person;
    }
}
