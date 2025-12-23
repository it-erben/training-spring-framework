# Übung 1: Actuator aktivieren und Endpoints freischalten

- Starte im Projekt `sb-advanced-actuator-task-start` und aktiviere den Starter `spring-boot-starter-actuator`, falls du ihn entfernt hast.
- Lege eine `application.yml` an und konfiguriere einen Actuator-Basispfad `/manage`.
- Exponiere die Endpoints `health,info,metrics,loggers` (optional auch `prometheus`) und zeige Health-Details immer an.
- Hinterlege sinnvolle `info.*`-Werte (z.B. Build-Version, Git-Commit) und teste die Endpoints per Browser oder `curl`.
- Aktiviere die Kubernetes Probes (`/manage/health/liveness`, `/manage/health/readiness`) und prüfe die Ausgabe im Vergleich zu `/manage/health`.

# Übung 2: Endpoints absichern

- Füge `spring-boot-starter-security` hinzu, falls noch nicht vorhanden, und definiere einen `SecurityFilterChain`, der nur für `EndpointRequest.toAnyEndpoint()` eine Admin-Rolle verlangt und alle anderen Requests erlaubt.
- Lege einen In-Memory-User mit Rolle `ADMIN` an und verifiziere den Zugriff auf `/manage/health` und `/manage/loggers`.
- Optional: Trenne Actuator und Anwendungs-Traffic, indem du den Management-Server auf Port `9000` verschiebst.

# Übung 3: Metriken und Log-Level zur Laufzeit

- Implementiere einen kleinen REST-Controller (z.B. `/api/orders`) und instrumentiere ihn mit Micrometer: nutze einen `Counter` für erfolgreiche Calls und einen `Timer` für die Dauer.
- Stelle sicher, dass die Metriken unter `/manage/metrics` sichtbar sind (z.B. `orders.processed` oder `http.server.requests` mit Tags).
- Nutze den Endpoint `/manage/loggers/{dein.package}` um den Log-Level des Controllers zur Laufzeit auf `DEBUG` zu setzen und prüfe die Log-Ausgabe.

# Übung 4: Custom HealthIndicator und eigener Endpoint

- Implementiere einen eigenen `HealthIndicator`, der einen simulierten externen Service prüft und zusätzliche Details (`withDetail`) liefert. Binde ihn so ein, dass er in `/manage/health` und den Readiness-Probes auftaucht.
- Erstelle einen Custom Actuator Endpoint (`@Endpoint(id = "featureToggle")`) mit `@ReadOperation` und `@WriteOperation`, über den du Feature-Flags anzeigen und ändern kannst. Teste den Endpoint über `/manage/featureToggle`.
- Denke daran, den Custom Endpoint für Web zu exponieren (`management.endpoints.web.exposure.include: ...,featureToggle` oder `*`).

# Bonus: Prometheus & Tracing

- Füge den Prometheus-Registry-Adapter `io.micrometer:micrometer-registry-prometheus` hinzu und stelle sicher, dass `/manage/prometheus` erreichbar ist.
- Optional: Integriere Micrometer Tracing mit `micrometer-tracing-bridge-otel` und einem Exporter (z.B. `opentelemetry-exporter-zipkin`). Prüfe, welche Trace-Header (`traceparent` vs. `b3`) du für bestehende Services konfigurieren musst.
