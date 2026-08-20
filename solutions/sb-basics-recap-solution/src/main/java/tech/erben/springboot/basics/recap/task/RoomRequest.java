package tech.erben.springboot.basics.recap.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Eingabeformat von {@code POST /api/rooms}. Die Constraints greifen über
 * {@code @Valid} im Controller; verletzt der Body sie, antwortet Spring mit
 * 400, bevor die Controller-Methode läuft.
 *
 * @param building darf fehlen — dann setzt der {@link RoomService} das Standardgebäude
 */
public record RoomRequest(@NotBlank String name,
                          @Positive int seats,
                          String building) {
}
