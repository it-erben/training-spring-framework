package tech.erben.springboot.basics.boot;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Macht die wirksame Konfiguration von außen sichtbar: {@code GET /info}
 * liefert die gebundenen {@link ShopProperties} und die aktiven Profile.
 * So lässt sich in der Demo direkt beobachten, welche Property-Quelle
 * gewonnen hat — Datei, Profil-Datei oder Kommandozeile.
 */
@RestController
public class ShopInfoController {

    /** Antwort-DTO — Jackson serialisiert das Record direkt als JSON. */
    record ShopInfo(String name, String currency, int pageSize, List<String> activeProfiles) {
    }

    private final ShopProperties shopProperties;
    private final Environment environment;

    public ShopInfoController(ShopProperties shopProperties, Environment environment) {
        this.shopProperties = shopProperties;
        this.environment = environment;
    }

    @GetMapping("/info")
    public ShopInfo info() {
        return new ShopInfo(
                shopProperties.name(),
                shopProperties.currency(),
                shopProperties.pageSize(),
                List.of(environment.getActiveProfiles()));
    }
}
