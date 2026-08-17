package tech.erben.springboot.basics.web;

import java.math.BigDecimal;

/**
 * Ein Buch aus dem Katalog der Buchhandlung — das interne Domänenmodell.
 * In der Live-Demo entsteht darum herum die REST-Schnittstelle mit eigenen
 * Request- und Response-DTOs.
 */
// TODO: Modul 02 — Schritt 2: nach außen geht künftig BookResponse (mit grossPrice) statt dieses Records
// TODO: Modul 02 — Schritt 5: BookRequest.toBook() erzeugt aus validierten Eingaben dieses Domänenobjekt
public record Book(String isbn, String title, BigDecimal netPrice) {
}
