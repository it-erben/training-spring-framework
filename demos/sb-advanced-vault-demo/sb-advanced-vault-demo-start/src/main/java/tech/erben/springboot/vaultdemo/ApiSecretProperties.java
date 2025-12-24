package tech.erben.springboot.vaultdemo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
public record ApiSecretProperties(String key) {}
