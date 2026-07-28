package tech.erben.springboot.basics.core;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Laeuft nach dem Start des Containers und gibt den Katalog mit
 * Bruttopreisen aus. Der {@link ObjectProvider} holt zwei
 * {@link PrototypeCounter}-Instanzen und macht so den Prototype-Scope
 * sichtbar.
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
            System.out.printf("  %s | %s | %s EUR (brutto)%n",
                    book.isbn(), book.title(), bookService.priceFor(book));
        }

        // Prototype-Scope: Jede Anfrage an den Container liefert eine neue Instanz.
        PrototypeCounter first = counterProvider.getObject();
        PrototypeCounter second = counterProvider.getObject();
        System.out.printf("Prototype-Scope: Instanz #%d und Instanz #%d — %s%n",
                first.instanceNumber(), second.instanceNumber(),
                first == second ? "dieselbe Instanz" : "zwei verschiedene Instanzen");
    }
}
