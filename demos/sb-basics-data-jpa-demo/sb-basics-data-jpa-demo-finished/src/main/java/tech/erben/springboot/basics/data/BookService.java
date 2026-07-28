package tech.erben.springboot.basics.data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fachlogik mit Transaktionsgrenzen. {@code @Transactional} sorgt dafuer,
 * dass jede Methode komplett oder gar nicht wirkt: Commit am normalen Ende,
 * Rollback bei einer RuntimeException.
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Hebt alle Preise um den Faktor an. Auffaellig: nirgendwo ein
     * {@code save()} — die geladenen Entities sind innerhalb der
     * Transaktion "managed", Hibernate erkennt die Aenderung per Dirty
     * Checking und schreibt beim Commit die UPDATE-Statements selbst.
     */
    @Transactional
    public void raisePrices(BigDecimal factor) {
        applyFactor(factor);
    }

    /**
     * Dieselbe Preisaenderung, aber danach fliegt absichtlich eine
     * {@link IllegalStateException} — die Rollback-Demo. Das {@code flush()}
     * zwingt Hibernate, die UPDATE-Statements sofort auszufuehren: Sie
     * erscheinen im Log, und trotzdem steht nach dem Rollback wieder der
     * alte Preis in der Datenbank.
     */
    @Transactional
    public void raisePricesAndFail(BigDecimal factor) {
        applyFactor(factor);
        bookRepository.flush();
        throw new IllegalStateException(
                "Absichtlicher Fehler nach der Preisaenderung — die Transaktion rollt zurueck");
    }

    private void applyFactor(BigDecimal factor) {
        bookRepository.findAll().forEach(book -> book.setNetPrice(
                book.getNetPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP)));
    }
}
