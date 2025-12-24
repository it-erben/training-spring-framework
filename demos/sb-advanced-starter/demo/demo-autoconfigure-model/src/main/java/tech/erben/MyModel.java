package tech.erben;

public record MyModel(String firstName, String lastName, Integer age) {

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

}
