package tech.erben.springboot.basics.web;

import java.math.BigDecimal;

/**
 * Ein Buch aus dem Katalog der Buchhandlung — das interne Domaenenmodell.
 * Nach aussen geht es nie direkt raus: Die REST-Schnittstelle uebersetzt
 * zwischen {@link BookRequest}, {@code Book} und {@link BookResponse}.
 */
public record Book(String isbn, String title, BigDecimal netPrice) {
}
