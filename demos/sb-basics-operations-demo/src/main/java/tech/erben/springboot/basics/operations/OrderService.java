package tech.erben.springboot.basics.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Erzeugt Beispielbestellungen der Buchhandlung. Die {@code debug}-Zeilen
 * hier sind der Kern der Demo: Bei dem konfigurierten Level {@code INFO}
 * bleiben sie unsichtbar — bis das Level zur Laufzeit über
 * {@code POST /actuator/loggers/tech.erben} auf {@code DEBUG} umgestellt
 * wird. Kein Neustart, kein Deployment.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    /** Eine Bestellung der Buchhandlung — Jackson serialisiert das Record direkt als JSON. */
    public record Order(long id, String bookTitle, int quantity) {
    }

    public List<Order> findAll() {
        log.debug("Stelle Beispielbestellungen zusammen");
        List<Order> orders = List.of(
                new Order(1, "Clean Code", 2),
                new Order(2, "Effective Java", 1),
                new Order(3, "Refactoring", 5));
        for (Order order : orders) {
            log.debug("Bestellung {}: {} x '{}'", order.id(), order.quantity(), order.bookTitle());
        }
        return orders;
    }
}
