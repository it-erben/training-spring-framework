package tech.erben.springboot.basics.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Konfigurationswerte aus {@code application.properties}. {@code @Value}
 * injiziert den Wert von {@code shop.name}; nach dem Doppelpunkt steht der
 * Default, falls die Property fehlt.
 */
@Component
public class ShopProperties {

    @Value("${shop.name:Buchhandlung Erben}")
    private String name;

    public String getName() {
        return name;
    }
}
