package tech.erben.springboot.basics.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Einstiegspunkt der Demo. {@code @SpringBootApplication} fasst drei
 * Annotationen zusammen: {@code @Configuration},
 * {@code @EnableAutoConfiguration} (Spring Boot konfiguriert anhand des
 * Classpaths z.&nbsp;B. den eingebetteten Tomcat) und
 * {@code @ComponentScan} ab diesem Package.
 * {@code @ConfigurationPropertiesScan} registriert zusätzlich alle
 * {@code @ConfigurationProperties}-Typen wie {@link ShopProperties} —
 * nötig, weil Records keine Stereotyp-Annotation tragen.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootDemoApplication.class, args);
    }
}
