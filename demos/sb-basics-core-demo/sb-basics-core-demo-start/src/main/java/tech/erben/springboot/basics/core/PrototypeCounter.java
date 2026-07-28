package tech.erben.springboot.basics.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Soll den Unterschied zwischen Singleton- und Prototype-Scope zeigen:
 * Im Prototype-Scope erzeugt der Container bei jeder Anfrage eine neue
 * Instanz — der statische Zaehler macht das sichtbar.
 */
// TODO: Modul 00 — als Spring-Bean deklarieren
public class PrototypeCounter {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final int instanceNumber = INSTANCES.incrementAndGet();

    public int instanceNumber() {
        return instanceNumber;
    }
}
