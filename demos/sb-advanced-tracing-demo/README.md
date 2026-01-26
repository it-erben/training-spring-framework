# Tracing Demo

Diese Demo zeigt Performance-Tracing in Spring Boot mit Micrometer,
OpenTelemetry Exporter und Jaeger 2 (OTLP HTTP).

## Struktur

```
sb-advanced-tracing-demo/
├── sb-advanced-tracing-demo-start/
└── sb-advanced-tracing-demo-finished/
```

## Demo starten

1. Jaeger 2 starten:

   ```bash
   docker run --rm --name jaeger \
     -p 16686:16686 \
     -p 4318:4318 \
     cr.jaegertracing.io/jaegertracing/jaeger:2.14.1 \
     --set receivers.otlp.protocols.http.endpoint=0.0.0.0:4318
   ```

2. Anwendung starten:

   ```bash
   cd sb-advanced-tracing-demo-finished
   mvn spring-boot:run
   ```

3. Requests senden:

   ```bash
   curl "http://localhost:8080/api/customers/1"
   curl -X POST "http://localhost:8080/api/customers/1/orders" \
     -H "Content-Type: application/json" \
     -d '{"description":"Express Delivery","amount":19.90}'
   ```

4. Jaeger UI oeffnen:

   ```text
   http://localhost:16686
   ```
