package tech.erben.springboot.basics.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Data-Demo. Anders als in Modul 02 gibt es keinen
 * Web-Server: Die Anwendung startet, der {@link SeedDataRunner} läuft
 * einmal durch, danach fährt der Kontext wieder herunter. Alles Sichtbare
 * passiert im Log — {@code spring.jpa.show-sql=true} zeigt jedes
 * SQL-Statement, das Hibernate erzeugt.
 */
@SpringBootApplication
public class DataDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataDemoApplication.class, args);
    }
}
