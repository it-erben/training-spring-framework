# Resilience4J Demo

Diese Demo zeigt die Integration von Resilience4J in eine Spring Boot Anwendung mit allen wichtigen Fault-Tolerance Patterns.

## Patterns

1. **Circuit Breaker** - Unterbricht Aufrufe zu fehlerhaften Services
2. **Retry** - Automatische Wiederholungsversuche bei transienten Fehlern
3. **Rate Limiter** - Begrenzt die Anzahl der Aufrufe pro Zeiteinheit
4. **Bulkhead** - Begrenzt parallele Aufrufe (Ressourcen-Isolation)
5. **Time Limiter** - Timeout für asynchrone Aufrufe

## Projektstruktur

```
sb-advanced-resilience4j-demo/
├── sb-advanced-resilience4j-demo-start/    # Ausgangspunkt für die Demo
└── sb-advanced-resilience4j-demo-finished/ # Fertige Implementierung
```

## Demo-Szenario

Ein Produkt-Service (`ProductService`) ruft einen externen Pricing-Service auf, um aktuelle Preise zu erhalten. Der externe Service wird mit WireMock simuliert.

```
[Client] -> [ProductController] -> [ProductService] -> [ExternalPricingClient] -> [External Pricing Service]
              (Rate Limiter)        (Bulkhead)         (Circuit Breaker, Retry, TimeLimiter)
```

## Demo durchführen

### 1. Anwendung starten

```bash
cd sb-advanced-resilience4j-demo-finished
mvn spring-boot:run
```

### 2. WireMock starten (in separatem Terminal)

Für die Demo wird ein externer Pricing-Service simuliert. Starte WireMock auf Port 8081:

```bash
# Option A: WireMock Standalone JAR
java -jar wiremock-standalone-3.10.0.jar --port 8081

# Option B: Docker
docker run -it --rm -p 8081:8080 wiremock/wiremock:3.10.0
```

### 3. WireMock Stubs konfigurieren

```bash
# Normaler Preis-Response
curl -X POST http://localhost:8081/__admin/mappings -d '{
  "request": {
    "method": "GET",
    "urlPathPattern": "/api/prices/.*"
  },
  "response": {
    "status": 200,
    "headers": {"Content-Type": "application/json"},
    "body": "{\"price\": 149.99}"
  }
}'
```

### 4. Produkt abrufen

```bash
curl http://localhost:8080/api/products/1
```

Erwartete Antwort:

```json
{
  "id": "1",
  "name": "Laptop",
  "price": 149.99,
  "priceSource": "external"
}
```

## Pattern-Demos

### Circuit Breaker Demo

1. **Normaler Betrieb** (Circuit CLOSED):

   ```bash
   curl http://localhost:8080/api/products/1
   ```

2. **Circuit Breaker Status prüfen**:

   ```bash
   curl http://localhost:8080/actuator/circuitbreakers
   ```

3. **Fehler simulieren** (WireMock auf 500 setzen):

   ```bash
   curl -X POST http://localhost:8081/__admin/mappings/reset
   curl -X POST http://localhost:8081/__admin/mappings -d '{
     "request": {"method": "GET", "urlPathPattern": "/api/prices/.*"},
     "response": {"status": 500, "body": "Error"}
   }'
   ```

4. **Mehrere Requests senden** (Circuit öffnet sich):

   ```bash
   for i in {1..10}; do curl -s http://localhost:8080/api/products/1 | jq '.priceSource'; done
   ```

   Nach einigen Fehlern wechselt `priceSource` zu "fallback".

5. **Circuit Breaker Status erneut prüfen**:

   ```bash
   curl -s http://localhost:8080/actuator/circuitbreakers | jq '.circuitBreakers.pricingService.state'
   ```

### Rate Limiter Demo

Der Rate Limiter ist auf 5 Requests pro Sekunde konfiguriert.

```bash
# Schnelle Requests senden
for i in {1..10}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/products/1
done
```

Einige Requests sollten `429 Too Many Requests` zurückgeben.

**Rate Limiter Status**:

```bash
curl http://localhost:8080/actuator/ratelimiters
```

### Bulkhead Demo

Der Bulkhead ist auf 3 parallele Aufrufe begrenzt.

```bash
# Parallele Requests (mit curl parallel oder xargs)
seq 10 | xargs -P 10 -I {} curl -s http://localhost:8080/api/products/1
```

**Bulkhead Status**:

```bash
curl http://localhost:8080/actuator/bulkheads
```

### Retry Demo

Retry ist auf 3 Versuche mit exponential backoff konfiguriert.

1. **Intermittierende Fehler simulieren** (WireMock Fault):

   ```bash
   curl -X POST http://localhost:8081/__admin/mappings/reset
   curl -X POST http://localhost:8081/__admin/mappings -d '{
     "request": {"method": "GET", "urlPathPattern": "/api/prices/.*"},
     "response": {
       "status": 200,
       "headers": {"Content-Type": "application/json"},
       "body": "{\"price\": 149.99}",
       "fault": "RANDOM_DATA_THEN_CLOSE"
     }
   }'
   ```

2. **Retry Events beobachten**:

   ```bash
   curl http://localhost:8080/actuator/retryevents
   ```

### Time Limiter Demo (Async Endpoint)

```bash
# Async Endpoint nutzen
curl http://localhost:8080/api/products/1/async

# Langsame Response simulieren
curl -X POST http://localhost:8081/__admin/mappings/reset
curl -X POST http://localhost:8081/__admin/mappings -d '{
  "request": {"method": "GET", "urlPathPattern": "/api/prices/.*"},
  "response": {
    "status": 200,
    "headers": {"Content-Type": "application/json"},
    "body": "{\"price\": 149.99}",
    "fixedDelayMilliseconds": 5000
  }
}'

# Request sollte nach 2s Timeout in Fallback gehen
curl http://localhost:8080/api/products/1/async
```

## Actuator Endpoints

| Endpoint                         | Beschreibung                         |
|----------------------------------|--------------------------------------|
| `/actuator/health`               | Health Status inkl. Circuit Breaker  |
| `/actuator/circuitbreakers`      | Circuit Breaker Status               |
| `/actuator/circuitbreakerevents` | Circuit Breaker Events               |
| `/actuator/ratelimiters`         | Rate Limiter Status                  |
| `/actuator/ratelimiterevents`    | Rate Limiter Events                  |
| `/actuator/retries`              | Retry Status                         |
| `/actuator/retryevents`          | Retry Events                         |
| `/actuator/bulkheads`            | Bulkhead Status                      |
| `/actuator/timelimiters`         | Time Limiter Status                  |
| `/actuator/prometheus`           | Prometheus Metrics                   |

## Konfiguration (application.yml)

Die wichtigsten Konfigurationsoptionen:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      pricingService:
        slidingWindowSize: 5        # Anzahl der Aufrufe für Berechnung
        failureRateThreshold: 50    # % Fehler zum Öffnen
        waitDurationInOpenState: 5s # Wartezeit im OPEN State

  retry:
    instances:
      pricingService:
        maxAttempts: 3              # Max Wiederholungen
        waitDuration: 500ms         # Wartezeit zwischen Versuchen

  ratelimiter:
    instances:
      productApi:
        limitForPeriod: 5           # Max Requests
        limitRefreshPeriod: 1s      # Zeitfenster

  bulkhead:
    instances:
      productService:
        maxConcurrentCalls: 3       # Max parallele Aufrufe

  timelimiter:
    instances:
      pricingService:
        timeoutDuration: 2s         # Timeout für async Aufrufe
```
