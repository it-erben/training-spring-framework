package tech.erben.springboot.basics.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Web-Demo. Durch {@code spring-boot-starter-web} startet
 * hier automatisch ein eingebetteter Tomcat auf Port 8080 — konfiguriert hat
 * ihn niemand, das erledigt die AutoConfiguration.
 */
// TODO: Modul 02 — Schritt 1: hier ist nichts zu ändern — der neue BookController liegt im selben Paket und wird vom Component-Scan automatisch gefunden
@SpringBootApplication
public class WebDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebDemoApplication.class, args);
    }
}
