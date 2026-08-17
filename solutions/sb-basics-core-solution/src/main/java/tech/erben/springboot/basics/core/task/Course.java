package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;

/**
 * Ein Kurs aus dem Schulungskatalog. Als Record unveränderlich —
 * Konstruktor, Zugriffsmethoden, equals/hashCode und toString kommen
 * ohne weiteren Code mit.
 */
public record Course(String code, String title, int seats, BigDecimal netFee) {
}
