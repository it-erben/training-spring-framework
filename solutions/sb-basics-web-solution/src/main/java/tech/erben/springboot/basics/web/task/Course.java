package tech.erben.springboot.basics.web.task;

import java.math.BigDecimal;

/**
 * Ein Kurs aus dem Schulungskatalog — das interne Domaenenmodell.
 * Nach aussen geht es nie direkt raus: Die REST-Schnittstelle uebersetzt
 * zwischen {@link CourseRequest}, {@code Course} und {@link CourseResponse}.
 */
public record Course(String code, String title, int seats, BigDecimal netFee) {
}
