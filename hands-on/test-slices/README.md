# Übungsaufgabe: Spring Boot Test Slices mit `@ControllerAdvice`

## Ziel der Übung

In dieser Übung lernt ihr, wie **Spring Boot Test Slices** eingesetzt werden, um schnelle und fokussierte Tests für
einzelne Schichten (JPA, MVC) zu schreiben.

---

## Fachliches Szenario

Ihr implementiert einen kleinen **Contact-Service**.

Ein `Contact` besteht aus:

- Vorname
- Nachname
- E-Mail-Adresse

Der Service soll:

- neue Contacts anlegen
- Contacts nach Nachnamen suchen

---

## Teil A – Implementierung (Grundlage für Tests)

Erstellt einen neuen Spring-Boot-Service mit diesen Abhängigkeiten:

- Spring Web
- Spring Data JPA
- H2 Database

### Entity `Contact`

- `id: Long`
- `firstName: String`
- `lastName: String`
- `email: String`

### DTOs

- `ContactCreateDto(firstName, lastName, email)`
- Optional: `ContactResponseDto` für die Antwort (oder die Entity, wenn ihr es simpel haltet).

### Repository `ContactRepository`

- `Optional<Contact> findByEmail(String email)`
- `List<Contact> findByLastNameStartingWithIgnoreCase(String prefix)` (oder ähnlich)

### Service `ContactService`

- `Contact create(ContactCreateDto dto)`
- `List<Contact> searchByLastNamePrefix(String prefix)`

### Controller `ContactController`

- `POST /contacts`
    - Request Body: `ContactCreateDto`
    - Response:
        - `201 Created` + Response-DTO/Entity
- `GET /contacts?lastNamePrefix=...`
    - Response: Liste von Contacts

---

## Teil B – `@DataJpaTest`

Schreibt einen `ContactRepositoryTest` mit `@DataJpaTest`.

Pflichttests:

1. `findByEmail_returnsContact_whenExists`
    - Given: Contact speichern
    - When: `findByEmail`
    - Then: gefunden, E-Mail korrekt

---

## Teil C – `@WebMvcTest`

Ziel: Nur die MVC-Schicht laden, Service mocken, HTTP/JSON prüfen.

Schreibt einen `ContactsControllerWebMvcTest` mit `@WebMvcTest(ContactController.class)`.

### Setup

- `@Autowired MockMvc`
- `@MockBean ContactService`

### Pflichttests

1. `postContacts_returns201_whenValid`
    - Given: POST mit validem JSON
    - When: Service mockt Rückgabe eines Contact/DTO
    - Then: `201`, Response enthält `email`
2. `getContacts_delegatesToService_andReturns200`
    - Given: Service liefert Liste
    - When: `GET /contacts?lastNamePrefix=Sm`
    - Then: `200`, Liste enthält erwartete Einträge
