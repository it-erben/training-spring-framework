package tech.erben.springboot.basics.core;

import java.time.Clock;

/**
 * Soll eine Fremdklasse als Bean bereitstellen: {@link Clock} stammt aus dem
 * JDK und kann nicht mit {@code @Component} annotiert werden.
 */
// TODO: Modul 00 — Schritt 3: mit @Configuration als Konfigurationsklasse deklarieren
public class ShippingConfig {

    // TODO: Modul 00 — Schritt 3: mit @Bean die Clock als Bean registrieren — @Component geht bei JDK-Klassen nicht
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
