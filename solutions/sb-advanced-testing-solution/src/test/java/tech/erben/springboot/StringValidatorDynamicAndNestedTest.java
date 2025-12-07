package tech.erben.springboot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Stack;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class StringValidatorDynamicAndNestedTest {

    private final StringValidator validator = new StringValidator();

    @TestFactory
    Stream<DynamicTest> dynamicPalindromeTests() {
        return Stream
            .of("racecar", "radar", "not a palindrome")
            .map(input ->
                DynamicTest.dynamicTest(
                    "Validate " + input,
                    () -> {
                        boolean isPalindrome = validator.isPalindrome(input);
                        if (input.contains("not")) {
                            assertFalse(isPalindrome);
                        } else {
                            assertTrue(isPalindrome);
                        }
                    }
                )
            );
    }

    @DisplayName("Ein Stack")
    @Nested
    class StackTests {

        Stack<Integer> stack = new Stack<>();

        @Test
        @DisplayName("ist initial leer")
        void isNew() {
            assertTrue(stack.isEmpty());
        }

        @Nested
        @DisplayName("nach dem Push eines Elements")
        class AfterPush {

            @Test
            @DisplayName("ist er nicht mehr leer")
            void isNotEmpty() {
                stack.push(1);
                assertFalse(stack.isEmpty());
            }
        }
    }
}
