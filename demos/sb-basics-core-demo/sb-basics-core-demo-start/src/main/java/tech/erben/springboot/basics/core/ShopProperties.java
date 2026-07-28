package tech.erben.springboot.basics.core;

/**
 * Konfigurationswerte aus {@code application.properties}. Der Wert von
 * {@code shop.name} soll spaeter injiziert werden.
 */
// TODO: Modul 00 — als Spring-Bean deklarieren
public class ShopProperties {

    // TODO: Modul 00 — als Spring-Bean deklarieren
    private String name = "Buchhandlung Erben";

    public String getName() {
        return name;
    }
}
