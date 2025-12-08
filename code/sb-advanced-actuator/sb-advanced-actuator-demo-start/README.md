# Actuator Demo Transcript

Setup: Starte im Projekt `sb-advanced-actuator-demo-start`. Ziel: Actuator aktivieren, absichern, Metriken und Custom Endpoints zeigen.

## 1. Actuator aktivieren & Endpoints freischalten
- Zeige `pom.xml`: `spring-boot-starter-actuator` ist bereits enthalten.
- Lege `application.yml` an (oder zeige vorhandene) mit Basispfad `/manage` und Exposure:
```yaml
management:
  endpoints:
    web:
      base-path: /manage
      exposure:
        include: health,info,metrics,loggers,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
info:
  app:
    name: Actuator Demo
    version: 1.0.0
```
- Starte die App und rufe `/manage/health`, `/manage/info`, `/manage/metrics` auf. Zeige den Unterschied zwischen `/health`, `/health/liveness`, `/health/readiness`.

## 2. Security für Actuator
- Ergänze `SecurityConfig`: `.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")`, rest `permitAll`.
- Füge In-Memory-User `admin/admin` hinzu und zeige Basic Auth gegen `/manage/health`.
- Optional: Setze `management.server.port=9000`, um Actuator auf eigenem Port zu trennen.

## 3. Laufzeit-Loglevel & Loggers-Endpoint
- Rufe `/manage/loggers/tech.erben.springboot.actuatordemo` auf, zeige aktuellen Level.
- Setze per `POST /manage/loggers/...` auf `DEBUG` und zeige Log-Ausgabe beim nächsten Request.

## 4. Micrometer Metrics im Code
- Zeige `OrderService`: `Counter orders.processed` und `Timer orders.processing.duration`.
- Erkläre @Timed am `POST /api/orders`. Führe Requests aus:
  - `curl -X POST http://localhost:8080/api/orders -d '{"product":"book"}' -H 'Content-Type: application/json'`
  - `curl http://localhost:8080/manage/metrics/orders.processed -u "admin:admin"`
  - `curl http://localhost:8080/manage/metrics/http.server.requests -u "admin:admin"`

## 5. Custom HealthIndicator
- Zeige `ExternalServiceHealthIndicator`: simuliert externen Service, liefert Details/Fehler.
- Betone, dass er unter `/manage/health` und den Probes erscheint.

## 6. Custom Endpoint
- Zeige `FeatureToggleEndpoint` mit `@Endpoint(id="featureToggle")`.
- `GET /manage/featureToggle` listet Flags, `POST /manage/featureToggle/{name}?enabled=true|false` ändert ein Flag.
- Demo: Schalte `externalService=false` und zeige, dass der HealthIndicator auf DOWN geht.

## 7. Prometheus & Tracing Hinweis
- Prometheus-Adapter ist eingebunden (`/manage/prometheus`). Zeige ein paar Beispielzeilen.
- Erwähne Bonus: `management.tracing.propagation.type=b3` für alte Services, sonst W3C TraceContext Default.
