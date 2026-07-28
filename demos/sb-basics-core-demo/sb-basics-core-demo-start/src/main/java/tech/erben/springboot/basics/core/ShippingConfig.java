package tech.erben.springboot.basics.core;

import java.time.Clock;

/**
 * Soll eine Fremdklasse als Bean bereitstellen: {@link Clock} stammt aus dem
 * JDK und kann nicht mit {@code @Component} annotiert werden.
 */
// TODO: Modul 00 — als Spring-Bean deklarieren
public class ShippingConfig {

    // TODO: Modul 00 — als Spring-Bean deklarieren
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
