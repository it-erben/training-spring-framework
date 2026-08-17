package tech.erben.springboot.basics.core.task;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lösung der Bonusaufgabe: Mit {@code @Scope("prototype")} erzeugt der
 * Container bei jeder Anfrage eine neue Instanz — der statische Zähler
 * macht das über {@link #instanceNumber()} sichtbar.
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
