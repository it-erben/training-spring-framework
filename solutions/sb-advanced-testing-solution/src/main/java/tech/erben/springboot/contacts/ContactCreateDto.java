package tech.erben.springboot.contacts;

public record ContactCreateDto(
        String firstName,
        String lastName,
        String email
) {
}
