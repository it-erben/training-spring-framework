package tech.erben;

/**
 * Simple value object embedded into {@link Person} documents.
 */
public record PhoneNumber(Integer countryCode, Integer number) {
}
