package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;

/**
 * Ein Kurs aus dem Schulungskatalog. Als Record unveränderlich.
 * Konstruktor, Zugriffsmethoden, equals/hashCode und toString entstehen
 * ohne weiteren Code.
 */
public record Course(String code, String title, int seats, BigDecimal netFee) {
}
