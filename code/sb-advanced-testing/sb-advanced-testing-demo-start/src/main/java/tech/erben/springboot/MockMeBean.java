package tech.erben.springboot;

import org.springframework.stereotype.Service;

public class MockMeBean {

    String mockMe() {
        return "not yet mocked";
    }

    String mockMeWithArguments(String arg) {
        return arg;
    }
}
