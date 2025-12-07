package tech.erben.springboot.vaultdemo;

import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class SecretController {

    private final DataSourceSecretProperties dataSourceSecrets;
    private final ApiSecretProperties apiSecrets;
    private final String passwordViaValue;
    private final String message;

    public SecretController(
        DataSourceSecretProperties dataSourceSecrets,
        ApiSecretProperties apiSecrets,
        @Value("${datasource.password:<not-set>}") String passwordViaValue,
        @Value("${app.message:Fallback aus application.yml}") String message
    ) {
        this.dataSourceSecrets = dataSourceSecrets;
        this.apiSecrets = apiSecrets;
        this.passwordViaValue = passwordViaValue;
        this.message = message;
    }

    @GetMapping("/secrets")
    public Map<String, String> secrets() {
        return Map.of(
            "datasourcePasswordViaConfigProperties",
            mask(dataSourceSecrets.password()),
            "datasourcePasswordViaValue",
            mask(passwordViaValue),
            "apiKey",
            mask(apiSecrets.key())
        );
    }

    @GetMapping("/secrets/raw")
    public Map<String, String> rawSecrets() {
        return Map.of(
            "datasourcePassword",
            defaultString(dataSourceSecrets.password()),
            "apiKey",
            defaultString(apiSecrets.key())
        );
    }

    @GetMapping("/message")
    public Map<String, String> message() {
        return Map.of("message", defaultString(message));
    }

    private String mask(String value) {
        String sanitized = defaultString(value);
        if ("<not-set>".equals(sanitized)) {
            return sanitized;
        }
        if (sanitized.length() <= 4) {
            return "****";
        }
        String tail = sanitized.substring(sanitized.length() - 4);
        return "*".repeat(Math.max(sanitized.length() - 4, 4)) + tail;
    }

    private String defaultString(String value) {
        return Objects.toString(value, "<not-set>");
    }
}
