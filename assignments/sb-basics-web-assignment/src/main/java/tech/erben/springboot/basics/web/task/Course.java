package tech.erben.springboot.basics.web.task;

import java.math.BigDecimal;

/**
 * Ein Kurs aus dem Schulungskatalog — das interne Domänenmodell.
 * Nach außen geht es nie direkt raus: Die REST-Schnittstelle übersetzt
 * zwischen {@code CourseRequest}, {@code Course} und {@code CourseResponse}
 * — genau diese beiden DTOs baut ihr in dieser Übung.
 */
public record Course(String code, String title, int seats, BigDecimal netFee) {
}
