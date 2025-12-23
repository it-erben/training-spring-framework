package tech.erben.springboot.vaultdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class SecretController {

    private final DataSourceSecretProperties dataSourceSecrets;
    private final ApiSecretProperties apiSecrets;
    private final String message;

    public SecretController(
        DataSourceSecretProperties dataSourceSecrets,
        ApiSecretProperties apiSecrets,
        @Value("${app.message:Demo ohne Vault}") String message
    ) {
        this.dataSourceSecrets = dataSourceSecrets;
        this.apiSecrets = apiSecrets;
        this.message = message;
    }

    @GetMapping("/secrets")
    public Map<String, String> secrets() {
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

    private String defaultString(String value) {
        return Objects.toString(value, "<not-set>");
    }
}
