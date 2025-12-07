package tech.erben.springboot.vaultdemo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasource")
public record DataSourceSecretProperties(String password) {}
