package tech.erben.springboot.basics.core;

import java.math.BigDecimal;

/**
 * Ein Buch aus dem Katalog der Buchhandlung. Records eignen sich fuer
 * unveraenderliche Datenklassen: Konstruktor, Getter, equals/hashCode und
 * toString kommen ohne weiteren Code mit.
 */
public record Book(String isbn, String title, BigDecimal netPrice) {
}
