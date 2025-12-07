package tech.erben.springboot;

import java.util.Set;

public class StringValidator {

    private static final Set<Character> VOWELS = Set.of('a', 'e', 'i', 'o', 'u');

    public boolean isPalindrome(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return new StringBuilder(normalized).reverse().toString().equals(normalized);
    }

    public int countVowels(String value) {
        if (value == null) {
            return 0;
        }
        int count = 0;
        for (char c : value.toLowerCase().toCharArray()) {
            if (VOWELS.contains(c)) {
                count++;
            }
        }
        return count;
    }

    public boolean isValidUsername(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches("^[a-zA-Z0-9._-]{3,16}$");
    }
}
