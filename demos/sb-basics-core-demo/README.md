# Demo: Spring Core — IoC, Dependency Injection, Beans und Scopes

Diese Demo zeigt den Spring-Container am Beispiel einer Buchhandlung:
Klassen werden als Beans deklariert, per Konstruktor-Injection verdrahtet,
Mehrdeutigkeiten mit `@Primary` und `@Qualifier` aufgeloest und der
Unterschied zwischen Singleton- und Prototype-Scope sichtbar gemacht.

## Varianten

| Modul | Inhalt |
| --- | --- |
| `sb-basics-core-demo-start` | Alle Klassen vorhanden, aber ohne Stereotyp-Annotationen — der Container findet nichts. Ausgangspunkt fuer die Live-Demo. |
| `sb-basics-core-demo-finished` | Vollstaendig verdrahtete Variante inklusive Tests. |

## Ablauf der Live-Demo

Die `-start`-Variante wird in dieser Reihenfolge verdrahtet — nach jedem
Schritt lohnt ein Neustart, um die Wirkung zu zeigen:

1. **Repository:** `InMemoryBookRepository` mit `@Repository` deklarieren.
   Der Container legt die Bean an, sie tut aber noch nichts Sichtbares.
2. **Service:** `BookService` mit `@Service` deklarieren. Der Konstruktor
   verlangt `BookRepository` und `PriceCalculator` — der Start schlaegt
   fehl, solange die Kalkulatoren keine Beans sind. Also
   `NetPriceCalculator` und `GrossPriceCalculator` mit `@Component`
   deklarieren.
3. **Runner:** `CatalogRunner` mit `@Component` deklarieren, dazu
   `ShippingConfig` (`@Configuration` + `@Bean Clock clock()`) und
   `ShopProperties` (`@Component` + `@Value("${shop.name:Buchhandlung Erben}")`).
   Jetzt erscheint beim Start der Katalog.
4. **Mehrdeutigkeit:** Zwei `PriceCalculator`-Beans — welcher gewinnt?
   `@Primary` auf `GrossPriceCalculator` setzt den Standard, die
   Setter-Methode in `BookService` holt sich per
   `@Qualifier("netPriceCalculator")` explizit den anderen.
5. **Scopes:** `PrototypeCounter` mit `@Component` und
   `@Scope("prototype")` deklarieren. Der `CatalogRunner` fordert zwei
   Instanzen an — die Ausgabe zeigt zwei verschiedene Instanznummern.
   Zum Vergleich `@Scope("prototype")` entfernen: dieselbe Instanz kommt
   zweimal.

## Starten

Beide Varianten sind normale Spring-Boot-Anwendungen ohne Web-Server —
sie geben ihre Ausgabe auf der Konsole aus und beenden sich danach.

```bash
cd sb-basics-core-demo-start
mvn spring-boot:run
```

```bash
cd sb-basics-core-demo-finished
mvn spring-boot:run
```

Die `-start`-Variante startet und beendet sich kommentarlos — es gibt noch
keine Beans. Die `-finished`-Variante gibt den Katalog mit Bruttopreisen
und den Scope-Vergleich aus.

## Tests

Nur die `-finished`-Variante hat Tests (die `-start`-Variante ist bewusst
unverdrahtet und wuerde jeden `@SpringBootTest` scheitern lassen):

```bash
cd sb-basics-core-demo-finished
mvn test
```
