package tech.erben.springboot.basics.core;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zeigt den Unterschied zwischen Singleton- und Prototype-Scope: Bei
 * {@code @Scope("prototype")} erzeugt der Container bei jeder Anfrage eine
 * neue Instanz — der statische Zaehler macht das sichtbar.
 */
@Component
@Scope("prototype")
public class PrototypeCounter {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final int instanceNumber = INSTANCES.incrementAndGet();

    public int instanceNumber() {
        return instanceNumber;
    }
}
