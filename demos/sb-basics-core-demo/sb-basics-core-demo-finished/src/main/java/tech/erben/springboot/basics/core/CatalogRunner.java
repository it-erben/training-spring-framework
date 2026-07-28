package tech.erben.springboot.basics.core;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Laeuft nach dem Start des Containers und gibt den Katalog mit Brutto-
 * und Nettopreisen aus. Der {@link ObjectProvider} holt zwei
 * {@link PrototypeCounter}-Instanzen und macht so den Prototype-Scope
 * sichtbar — {@code getIfAvailable()} statt {@code getObject()}, damit der
 * Runner auch laeuft, solange {@link PrototypeCounter} in der Live-Demo
 * noch keine Bean ist.
 */
@Component
public class CatalogRunner implements CommandLineRunner {

    private final BookService bookService;
    private final ShopProperties shopProperties;
    private final Clock clock;
    private final ObjectProvider<PrototypeCounter> counterProvider;

    public CatalogRunner(BookService bookService,
                         ShopProperties shopProperties,
                         Clock clock,
                         ObjectProvider<PrototypeCounter> counterProvider) {
        this.bookService = bookService;
        this.shopProperties = shopProperties;
        this.clock = clock;
        this.counterProvider = counterProvider;
    }

    @Override
    public void run(String... args) {
        System.out.printf("Katalog von %s (Stand: %s)%n",
                shopProperties.getName(), LocalDate.now(clock));

        for (Book book : bookService.findAll()) {
            System.out.printf("  %s | %s | %s EUR brutto / %s EUR netto%n",
                    book.isbn(), book.title(),
                    bookService.priceFor(book), bookService.netPriceFor(book));
        }

        // Prototype-Scope: Jede Anfrage an den Container liefert eine neue
        // Instanz. getIfAvailable() liefert null, solange PrototypeCounter
        // keine Bean ist — so startet jeder Zwischenschritt der Live-Demo.
        PrototypeCounter first = counterProvider.getIfAvailable();
        if (first == null) {
            System.out.println("Prototype-Demo noch nicht aktiv (PrototypeCounter ist keine Bean).");
            return;
        }
        PrototypeCounter second = counterProvider.getObject();
        System.out.printf("Prototype-Scope: Instanz #%d und Instanz #%d — %s%n",
                first.instanceNumber(), second.instanceNumber(),
                first == second ? "dieselbe Instanz" : "zwei verschiedene Instanzen");
    }
}
