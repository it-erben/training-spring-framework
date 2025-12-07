package tech.erben.reactive;

import com.github.javafaker.Faker;
import java.util.Locale;

public class PersonGenerator {

    public static Person randomPerson() {
        var faker = Faker.instance(Locale.GERMAN);
        Person person = new Person();
        person.setFirstName(faker.name().firstName());
        person.setLastName(faker.name().lastName());
        return person;
    }
}
