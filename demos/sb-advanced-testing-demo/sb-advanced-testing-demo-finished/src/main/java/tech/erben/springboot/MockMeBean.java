package tech.erben.springboot;

public class MockMeBean {

    String mockMe() {
        return "not yet mocked";
    }

    String mockMeWithArguments(String arg) {
        return arg;
    }
}
