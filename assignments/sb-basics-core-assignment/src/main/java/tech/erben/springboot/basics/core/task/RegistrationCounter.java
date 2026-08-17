package tech.erben.springboot.basics.core.task;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zählt, die wievielte Instanz dieser Klasse der Container erzeugt hat.
 * Nur für die Bonusaufgabe relevant: Als Singleton (Standard) liefert
 * jede Injektion dieselbe Instanz. Mit {@code @Scope("prototype")}
 * erzeugt der Container bei jeder Anfrage eine neue.
 */
// TODO Bonusaufgabe: als Spring-Bean mit Prototype-Scope deklarieren
public class RegistrationCounter {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final int instanceNumber = INSTANCES.incrementAndGet();

    public int instanceNumber() {
        return instanceNumber;
    }
}
