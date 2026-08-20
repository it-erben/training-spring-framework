---
marp: true
theme: default
header: Spring Boot Advanced
footer: Alexander Erben
paginate: true
---
<!-- Dichte-Stufen gegen Folienueberlauf, siehe tools/check-slide-overflow.mjs -->
<style>
section.dense { font-size: 24.5px; }
section.denser { font-size: 21px; }
</style>

# Wiederholung: ein Service von Anfang an

---

## Worum es geht

In Zweierteams baut ihr ein neues Projekt von Grund auf.

---

## Fachdomäne

Ein Raum hat einen Namen, eine Platzzahl und ein Gebäude.

| Endpunkt                     | Verhalten                             |
|------------------------------|---------------------------------------|
| `GET /api/rooms`             | alle Räume                            |
| `GET /api/rooms?minSeats=20` | nur Räume ab dieser Platzzahl         |
| `GET /api/rooms/{id}`        | ein Raum, unbekannte ID → 404         |
| `POST /api/rooms`            | legt an, 201 mit `Location`-Header    |

---

## Schritt 1: Projekt erzeugen

Auf [start.spring.io](https://start.spring.io): Maven, Java 21, Spring Boot 4.0.x, Packaging Jar.

Dependencies: **Spring Web**, **Spring Data JPA**, **H2 Database**, **Validation**

* Archiv entpacken, in der IDE öffnen, `mvn spring-boot:run` starten.

**Fertig, wenn:** Der Log meldet `Tomcat started on port 8080` und `Started ...Application`.

---

## Schritt 2: Entity und Datenbank

* Legt eine Entity `Room` an: `id` (generiert), `name` (eindeutig), `seats`, `building`.
* JPA braucht einen parameterlosen Konstruktor, der `protected` sein darf.
* Tragt in `application.properties` die H2-URL und `spring.jpa.hibernate.ddl-auto` ein.
* Setzt zusätzlich `spring.jpa.show-sql=true`, sonst bleibt das erzeugte Schema unsichtbar.

**Ihr seid hier fertig, wenn:** Der Log beim Start `create table room (...)` zeigt.

---

## Schritt 3: Repository

* Schreibt ein Interface `RoomRepository`, das von `JpaRepository<Room, Long>` erbt.
* Ergänzt eine Methode, die alle Räume ab einer Mindestplatzzahl liefert.
* Eine Implementierung schreibt ihr nicht, Spring Data leitet die Abfrage aus dem Methodennamen ab.

**Checkpoint:** Die Anwendung startet weiterhin. Ein Name, den Spring Data nicht auflösen kann, lässt den Kontext beim Start scheitern.

---

## Schritt 4: Service

* `RoomService` als Bean, welche das Repository per **Konstruktor**-Injection erhält.
* Methoden: alle Räume, Räume ab Mindestplatzzahl, ein Raum per ID, Raum anlegen.
* Unbekannte ID soll zu einer eigenen `RoomNotFoundException` führen, nicht zu `null`.
* Schreibende Methode mit `@Transactional`.

**Fertig, wenn:** Der Kontext startet und der Service die Beans injiziert bekommt.

---

## Schritt 5: Controller

* `RoomController` mit `@RestController` und `@RequestMapping("/api/rooms")`.
* Die vier Endpunkte aus der Fachdomäne am Anfang dieses PDFs. Der optionale Filter übergebt ihr als `@RequestParam`.
* `POST` antwortet mit **201** und `Location`-Header, nicht mit 200.
* Die `RoomNotFoundException` wird über einen `@RestControllerAdvice` zu **404**.

---

## Schritt 6: Tests

* Ein `@DataJpaTest` für die Derived Query: Räume speichern, Abfrage prüfen.
* Ein `@WebMvcTest` für den Controller, mit `@MockitoBean` für den Service.
* Optional ein Unit-Test für die Service-Regel, ganz ohne Spring-Kontext.

**Fertig, wenn:** `mvn test` grün ist.

---

## Bonus

* **Validierung:** `@NotBlank` und `@Positive` am Request-Record, `@Valid` am Controller-Parameter. Leerer Name → 400.
* **Konfiguration:** ein `@ConfigurationProperties`-Record für ein Standardgebäude, das der Service setzt, wenn die Angabe fehlt. Der Component-Scan übersieht solche Typen — ohne `@ConfigurationPropertiesScan` an der Application-Klasse startet der Kontext nicht.
* **Betrieb:** `spring-boot-starter-actuator` einbinden und `/actuator/health` freischalten.
* **Logging:** eine `log.info`-Zeile beim Anlegen eines Raums.
