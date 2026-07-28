package tech.erben.springboot.basics.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Soll den Unterschied zwischen Singleton- und Prototype-Scope zeigen:
 * Im Prototype-Scope erzeugt der Container bei jeder Anfrage eine neue
 * Instanz — der statische Zaehler macht das sichtbar. Hier fehlen zwei
 * Annotationen: die Bean-Deklaration und der Scope.
 */
// TODO: Modul 00 — Schritt 5: mit @Component als Bean deklarieren
// TODO: Modul 00 — Schritt 5: mit @Scope("prototype") pro Anfrage eine neue Instanz liefern lassen
public class PrototypeCounter {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final int instanceNumber = INSTANCES.incrementAndGet();

    public int instanceNumber() {
        return instanceNumber;
    }
}
