package tech.erben.springboot.basics.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ein bewusst schlichter Endpoint, der bei jedem Aufruf auf {@code info}
 * und {@code debug} loggt. So lässt sich am selben Request beobachten,
 * welche Zeilen das konfigurierte Log-Level durchlässt — und was sich
 * ändert, sobald der Actuator das Level zur Laufzeit umstellt.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderService.Order> orders() {
        log.info("Bestellübersicht angefragt");
        List<OrderService.Order> orders = orderService.findAll();
        log.debug("Liefere {} Bestellungen aus", orders.size());
        return orders;
    }
}
