package tech.erben.springboot.basics.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typsichere Konfiguration: Alle Properties mit dem Präfix {@code shop}
 * werden per Constructor-Binding in dieses Record gebunden —
 * {@code shop.page-size} landet dank Relaxed Binding im Parameter
 * {@code pageSize}. Anders als {@code @Value} an einzelnen Feldern gibt es
 * hier einen zentralen, unveränderlichen Konfigurationsblock, der sich
 * injizieren und testen lässt.
 */
@ConfigurationProperties("shop")
public record ShopProperties(String name, String currency, int pageSize) {
}
