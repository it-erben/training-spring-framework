package tech.erben.springboot.basics.web;

import java.math.BigDecimal;

/**
 * Ein Buch aus dem Katalog der Buchhandlung — das interne Domaenenmodell.
 * In der Live-Demo entsteht darum herum die REST-Schnittstelle mit eigenen
 * Request- und Response-DTOs.
 */
public record Book(String isbn, String title, BigDecimal netPrice) {
}
