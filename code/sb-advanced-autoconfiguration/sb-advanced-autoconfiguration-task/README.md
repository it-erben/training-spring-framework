# Aufgabe 1

Ziel dieser Aufgabe ist es, sich mit Spring Conditional vertraut zu machen. Ihr findet in diesem Projekt ein Interface `OperatingSystem` mit drei Implementierungen, die systemabhängig den Namen des aktuellen Betriebssystems zurückgeben.

1. Erstelle drei Implementierungen des Interfaces `org.springframework.context.annotation.Conditional` und nenne sie `MacCondition`, `LinuxCondition` und `WindowsCondition`. Jede Klasse soll die Methode `matches` aus dem Interface `Condition` überschreiben und überprüfen, ob das aktuelle Betriebssystem dem jeweiligen Betriebssystem entspricht.

2. Erstelle eine `@Configuration` annotierte Klasse namens `OperatingSystemConfiguration`. In dieser Klasse sollen drei mit `@Bean` annotierte Methoden erstellt werden, die jeweils eine der obigen Condition-Klassen verwenden. Jede Methode soll eine Instanz der entsprechenden `OperatingSystem`-Implementierung zurückgeben, die ihr in diesem Projekt vorfindet.

3. Ersetze die Hauptklasse (`OsInfoApplication`) durch folgenden Inhalt:

```
@SpringBootApplication
public class OsInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsInfoApplication.class, args);
    }
}
```

# Aufgabe 2

1. Importiere den Spring Boot Starter für Tomcat, indem du die entsprechende Abhängigkeit in deine `pom.xml` (Maven) einfügst.

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
   <version>3.5.8</version>
</dependency>
```

1. Implementiere einen `@RestController`, der auf den Pfad `/os` hört und das Ergebnis von `writeOsInfo` zurückgibt

> Hier ist eine Vorlage für einen Rest-Controller in Spring:
>
> ```java
> @RestController
> @RequestMapping("/api")
> public class MyController {
>     @GetMapping
>     public String foo() {
>         return "bar";
>     }
> }
> ```

1. Starte die Anwendung und teste den Controller, indem du auf `http://localhost:8080/os` zugreifst. Überprüfe, ob
