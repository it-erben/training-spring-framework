package tech.erben.springboot.webdemo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class AllowedCategoryValidator
    implements ConstraintValidator<AllowedCategory, String> {

    private static final Set<String> ALLOWED = Set.of(
        "technology",
        "fiction",
        "science",
        "business",
        "children"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return ALLOWED.contains(value.toLowerCase(Locale.ROOT));
    }
}
