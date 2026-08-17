package tech.erben.springboot.basics.web.task;

import java.math.BigDecimal;

/**
 * Ein Kurs aus dem Schulungskatalog — das interne Domänenmodell.
 * Nach außen geht es nie direkt raus: Die REST-Schnittstelle übersetzt
 * zwischen {@link CourseRequest}, {@code Course} und {@link CourseResponse}.
 */
public record Course(String code, String title, int seats, BigDecimal netFee) {
}
