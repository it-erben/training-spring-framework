# Übungsaufgabe: Actuator und Micrometer

## Ziel

Ihr erweitert eine kleine CRUD-Anwendung um Observability:

1. Actuator-Endpunkte freischalten.
2. Eigene Micrometer-Metriken einführen.
3. Den Prometheus-Endpunkt gezielt absichern.

Die Dependencies für Actuator, Prometheus und Security sind bereits enthalten.
Die Konfiguration in `application.properties` und Teile der Security fehlen aber
noch.

## Fachliches Szenario

Ihr betreibt einen kleinen Product-Catalog-Service.

Es gibt bereits Endpunkte unter `/products` für:

- Anlegen
- Suchen/Listen
- Lesen per ID
- Aktualisieren
- Löschen

## Teil A: Actuator aktivieren

Ergänzt in `src/main/resources/application.properties` mindestens:

- `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
- `management.endpoint.health.show-details=always`
- `management.metrics.tags.application=${spring.application.name}`

### Checks

1. Startet die Anwendung.
2. Ruft diese Endpunkte im Browser oder per `curl` auf:
   `/actuator/health`
   `/actuator/metrics`
   `/actuator/metrics/http.server.requests`
   `/actuator/prometheus`

### Kurz erklärt: Wozu `/actuator/prometheus`?

Prometheus „scraped" diesen Endpunkt regelmäßig. Das heißt: Prometheus ruft ihn
in einem festen Intervall auf, liest das Textformat und speichert die Metriken
für Abfragen, Dashboards und Alerts.

## Teil B: Eigene Micrometer-Metriken

Führt in `ProductService` eigene Metriken ein.

### Anforderungen

Implementiert mindestens diese Metriken:

1. Counter `products.creations`
2. Counter `products.rejected`
3. Timer `products.search`
4. Gauge `products.count`

### Hinweise zur Implementierung

- Injiziert ein `MeterRegistry`.
- Legt Counter und Timer im Konstruktor an.
- Messt die Dauer der `list(...)`-Methode mit einem Timer.
- Erhöht `products.creations` nach erfolgreichem Anlegen.
- Erhöht `products.rejected` bei Duplicate-Fehlern.
- Für den Gauge könnt ihr eine `AtomicInteger` verwenden und den Wert nach
  `create`, `update` und `delete` setzen.
- Alternativ könnt ihr `repository.count()` referenzieren.

### Checks

1. Führt mehrere Requests gegen `/products` aus.
2. Prüft die Metriken:
   `/actuator/metrics/products.creations`
   `/actuator/metrics/products.rejected`
   `/actuator/metrics/products.search`
   `/actuator/metrics/products.count`

## Teil C: Prometheus-Endpunkt absichern (BASIC AUTH)

Ziel: `/actuator/prometheus` soll nur mit festen Credentials per BASIC AUTH
aufrufbar sein. Alle anderen Endpunkte dürfen offen bleiben.

Arbeitet in `SecurityConfig`.

### Vorgehen mit Snippets (bitte übertragen)

Übertragt die folgenden Snippets in die vorhandene Klasse. Achtet darauf, die
benötigten Imports zu ergänzen.

Benötigte Imports:

- Spring-Boot-Actuator-Security-Package:
  `org.springframework.boot.security.autoconfigure.actuate.web.servlet`
- Klasse: `EndpointRequest`
- `org.springframework.security.config.Customizer`
- `org.springframework.security.core.userdetails.User`
- `org.springframework.security.core.userdetails.UserDetails`
- `org.springframework.security.core.userdetails.UserDetailsService`
- `org.springframework.security.provisioning.InMemoryUserDetailsManager`

#### Snippet 1: `SecurityFilterChain`

Ersetzt den Inhalt der bestehenden Methode durch dieses Snippet:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                EndpointRequest.to("prometheus", "metrics")
            )
            .hasRole("PROMETHEUS")
            .anyRequest()
            .permitAll()
        )
        .httpBasic(Customizer.withDefaults());

    return http.build();
}
```

#### Snippet 2: In-Memory-User

Fügt zusätzlich diesen Bean in `SecurityConfig` ein:

```java
@Bean
UserDetailsService userDetailsService() {
    UserDetails prometheusUser = User
        .withUsername("prometheus")
        .password("{noop}prometheus-secret")
        .roles("PROMETHEUS")
        .build();

    return new InMemoryUserDetailsManager(prometheusUser);
}
```

Warum `{noop}`? Damit ihr für die Übung kein Password-Encoding konfigurieren
müsst.

### Checks

1. Ruft `/actuator/prometheus` und `/actuator/metrics` ohne Credentials auf.
   Erwartung: jeweils `401`.
2. Ruft beide Endpunkte mit BASIC AUTH auf. Erwartung: jeweils `200`.

Checks mit `curl`:

```bash
# Ohne Credentials (erwartet: 401)
curl -i http://localhost:8080/actuator/prometheus
curl -i http://localhost:8080/actuator/metrics

# Mit BASIC AUTH (erwartet: 200)
curl -i -u prometheus:prometheus-secret \
  http://localhost:8080/actuator/prometheus
curl -i -u prometheus:prometheus-secret \
  http://localhost:8080/actuator/metrics
```

### Bonus

- Verwendet `EndpointRequest` für Actuator-Endpunkte.
- Import-Package:
  `org.springframework.boot.security.autoconfigure.actuate.web.servlet`
- Klasse: `EndpointRequest`
- Sichert zusätzlich `/actuator/metrics` ab.
