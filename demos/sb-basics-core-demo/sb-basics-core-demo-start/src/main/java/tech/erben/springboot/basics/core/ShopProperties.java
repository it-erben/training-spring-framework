package tech.erben.springboot.basics.core;

/**
 * Konfigurationswerte aus {@code application.properties}. Der Wert von
 * {@code shop.name} soll später injiziert werden.
 */
// TODO: Modul 00 — Schritt 3: mit @Component als Bean deklarieren
public class ShopProperties {

    // TODO: Modul 00 — Schritt 3: shop.name per @Value("${shop.name:Buchhandlung Erben}") injizieren (Initialisierung entfernen)
    private String name = "Buchhandlung Erben";

    public String getName() {
        return name;
    }
}
