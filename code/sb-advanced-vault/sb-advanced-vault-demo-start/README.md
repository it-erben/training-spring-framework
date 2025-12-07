# Vault Demo Transcript

## 1. POM zeigen
- `spring-cloud-starter-vault-config` und `spring-cloud-starter-actuator` sind bereits eingebunden.
- Controller nutzt `@ConfigurationProperties` und (im Endzustand) `@RefreshScope`, um Secrets nach `/actuator/refresh` neu zu laden.

## 2. Vault im Dev-Modus starten (lokal)
```
vault server -dev -dev-root-token-id=root
```
Neues Terminal mit den gleichen ENV-Variablen öffnen.

Secrets für die Demo schreiben:
```
export VAULT_ADDR=http://127.0.0.1:8200
export VAULT_TOKEN=root
vault kv put secret/vault-demo datasource.password=superSecret api.key=abc123 app.message="Hallo aus Vault"
vault kv get secret/vault-demo
```

## 3. Spring Config vorbereiten
Im Startprojekt in `src/main/resources/application.yml` die Vault-Config ergänzen (Endzustand bereits in `finished` vorhanden):
```yaml
spring:
  config:
    import: optional:vault://
  cloud:
    vault:
      uri: ${VAULT_ADDR:http://127.0.0.1:8200}
      authentication: token
      token: ${VAULT_TOKEN:root}
      kv:
        enabled: true
        backend: secret
        application-name: ${spring.application.name}
      fail-fast: false
management:
  endpoints:
    web:
      exposure:
        include: health,info,refresh,env
```
Optional: Fallbacks in `datasource.password`, `api.key` und `app.message` lassen, damit die App auch ohne laufenden Vault startet.

## 4. Anwendung starten
```
mvn spring-boot:run
```

## 5. Secrets abrufen
- Ohne Vault-Config (vor Schritt 3) zeigt `http://localhost:8080/secrets` die lokalen Defaults.
- Mit Vault-Config:
```
curl http://localhost:8080/secrets          # Maskierte Ausgabe
curl http://localhost:8080/secrets/raw      # Für die Demo: Klartext (nicht in Prod!)
curl http://localhost:8080/message
```

## 6. Live-Update demonstrieren
```
vault kv put secret/vault-demo datasource.password=newSecret api.key=newKey app.message="Jetzt live geändert"
curl -X POST http://localhost:8080/actuator/refresh
curl http://localhost:8080/secrets/raw
curl http://localhost:8080/message
```
Hinweis: `/actuator/env` ist freigeschaltet; sensible Keys werden maskiert.
