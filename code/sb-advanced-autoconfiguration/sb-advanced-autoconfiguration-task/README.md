# Aufgabe 1
Ziel dieser Aufgabe ist es, sich mit Spring Conditional vertraut zu machen. Ihr findet in diesem Projekt ein Interface `OperatingSystem` mit drei Implementierungen, die systemabhängig den Namen des aktuellen Betriebssystems zurückgeben.

1. Erstelle drei Implementierungen des Interfaces `org.springframework.context.annotation.Conditional` und nenne sie `MacCondition`, `LinuxCondition` und `WindowsCondition`. Jede Klasse soll die Methode `matches` aus dem Interface `Condition` überschreiben und überprüfen, ob das aktuelle Betriebssystem dem jeweiligen Betriebssystem entspricht.

2. Erstelle eine `@Configuration` annotierte Klasse namens `OperatingSystemConfiguration`. In dieser Klasse sollen drei mit `@Bean` annotierte Methoden erstellt werden, die jeweils eine der obigen Condition-Klassen verwenden. Jede Methode soll eine Instanz der entsprechenden `OperatingSystem`-Implementierung zurückgeben, die ihr in diesem Projekt vorfindet.

3. Erstelle in der main-Methode einen `AnnotationConfigApplicationContext` mit der `OperatingSystemConfiguration`-Klasse, hole dir eine Bean des `OperatingSystem`-Interfaces aus dem Kontext und rufe die `writeOsInfo`-Methode auf. Prüfe, dass alles erwartungsgemäß funktioniert.

# Aufgabe 2

1. Importiere den Spring Boot Starter für Tomcat, indem du die entsprechende Abhängigkeit in deine `pom.xml` (Maven) einfügst.
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
   <version>3.2.0</version>
</dependency>
```

2. Implementiere ein Servlet, das auf `/os` auf Port 8080 hört und das Ergebnis von `writeOsInfo` zurückgibt. Du kannst das `HttpServlet` von Java verwenden und es mit Spring Boot integrieren. Beachte dabei die folgenden Schritte:

   a. Erstelle eine Klasse, die von `HttpServlet` erbt und überschreibe die `doGet`-Methode.

   b. In der `doGet`-Methode, rufe die `writeOsInfo`-Methode des `OperatingSystem`-Interfaces auf und gib das Ergebnis als Antwort zurück.

   c. Erstelle eine `@Configuration`-Klasse, in der du das Servlet mit Spring Boot registrierst. Verwende dazu die `ServletRegistrationBean`-Klasse und registriere das Servlet unter dem Pfad `/os`.

   d. Stelle sicher, dass das Servlet beim Start der Anwendung geladen wird, indem du die `setLoadOnStartup`-Methode der `ServletRegistrationBean`-Klasse aufrufst.

3. Starte die Anwendung und teste das Servlet, indem du auf `http://localhost:8080/os` zugreifst. Überprüfe, ob das Ergebnis von `writeOsInfo` korrekt angezeigt wird. 