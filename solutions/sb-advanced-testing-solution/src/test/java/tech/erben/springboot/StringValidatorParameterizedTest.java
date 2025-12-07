package tech.erben.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class StringValidatorParameterizedTest {

    private final StringValidator validator = new StringValidator();

    @ParameterizedTest
    @ValueSource(strings = { "racecar", "radar", "Able was I ere I saw Elba" })
    void detectsPalindromes(String candidate) {
        assertTrue(validator.isPalindrome(candidate));
    }

    @ParameterizedTest
    @CsvSource({ "Java,      2", "Kotlin,    2", "Groovy,    2", "SPRING,    1" })
    void countsVowels(String input, int expectedCount) {
        assertEquals(expectedCount, validator.countVowels(input));
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void validatesUsernames(String username, boolean expectedValid) {
        assertEquals(expectedValid, validator.isValidUsername(username));
    }

    static Stream<Arguments> usernameProvider() {
        return Stream.of(
            Arguments.of("alex", true),
            Arguments.of("a", false),
            Arguments.of("with space", false),
            Arguments.of("user_123", true)
        );
    }
}
