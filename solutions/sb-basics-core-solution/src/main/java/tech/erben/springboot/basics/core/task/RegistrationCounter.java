package tech.erben.springboot.basics.core.task;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loesung der Bonusaufgabe: Mit {@code @Scope("prototype")} erzeugt der
 * Container bei jeder Anfrage eine neue Instanz — der statische Zaehler
 * macht das ueber {@link #instanceNumber()} sichtbar.
 */
@Component
@Scope("prototype")
public class RegistrationCounter {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final int instanceNumber = INSTANCES.incrementAndGet();

    public int instanceNumber() {
        return instanceNumber;
    }
}
