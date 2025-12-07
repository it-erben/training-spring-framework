package tech.erben;

import java.util.Objects;

public class MyModel {

    private final String firstName;
    private final String lastName;
    private final Integer age;

    public MyModel(String firstName, String lastName, Integer age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getAge() {
        return age;
    }

    @Override
    public String toString() {
        return (
            "MyModel{" +
            "firstName='" +
            firstName +
            '\'' +
            ", lastName='" +
            lastName +
            '\'' +
            ", age=" +
            age +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyModel myModel = (MyModel) o;
        return (
            Objects.equals(firstName, myModel.firstName) &&
            Objects.equals(lastName, myModel.lastName) &&
            Objects.equals(age, myModel.age)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, age);
    }
}
