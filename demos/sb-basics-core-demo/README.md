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

Die `-start`-Variante wird in dieser Reihenfolge verdrahtet. Jeder
Zwischenzustand startet — nach jedem Schritt lohnt ein Neustart, um die
Wirkung zu zeigen:

1. **Repository:** `InMemoryBookRepository` mit `@Repository` deklarieren.
   Der Container legt die Bean an, sie tut aber noch nichts Sichtbares.
2. **Service und Kalkulatoren:** `BookService` mit `@Service` deklarieren.
   Der Konstruktor verlangt ein `BookRepository` und **zwei**
   `PriceCalculator`-Parameter. Also `NetPriceCalculator` mit
   `@Component("netPriceCalculator")` und `GrossPriceCalculator` mit
   `@Component("grossPriceCalculator")` **und sofort `@Primary`**
   deklarieren: Ohne `@Primary` kann der Container die unqualifizierten
   Parameter bei zwei Beans desselben Typs nicht aufloesen, und der Start
   bricht ab (`expected single matching bean but found 2`). Sichtbar
   passiert noch nichts — es gibt noch keinen Runner.
3. **Runner:** `CatalogRunner` mit `@Component` deklarieren, dazu
   `ShippingConfig` (`@Configuration` + `@Bean Clock clock()`) und
   `ShopProperties` (`@Component` + `@Value("${shop.name:Buchhandlung Erben}")`).
   Jetzt erscheint beim Start der Katalog — beide Preisspalten zeigen
   denselben Bruttowert, denn noch bekommen beide Konstruktor-Parameter
   die `@Primary`-Bean. Die letzte Zeile meldet
   `Prototype-Demo noch nicht aktiv` (kommt in Schritt 5).
4. **Mehrdeutigkeit:** `@Qualifier("netPriceCalculator")` an den dritten
   Konstruktor-Parameter von `BookService` setzen. Jetzt stehen beide
   Aufloesungswege in einer Signatur nebeneinander: Der unqualifizierte
   Parameter bekommt die `@Primary`-Bean, der qualifizierte explizit die
   andere — die Netto-Spalte zeigt ab jetzt echte Nettopreise. Wer das
   Fehlerbild aus Schritt 2 zeigen will: `@Primary` kurz entfernen und
   starten, danach wieder einsetzen.
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
keine Beans. Die `-finished`-Variante gibt den Katalog mit Brutto- und
Nettopreisen sowie den Scope-Vergleich aus.

## Tests

Nur die `-finished`-Variante hat Tests (die `-start`-Variante ist bewusst
unverdrahtet und wuerde jeden `@SpringBootTest` scheitern lassen):

```bash
cd sb-basics-core-demo-finished
mvn test
```
