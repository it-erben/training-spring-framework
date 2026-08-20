package tech.erben.springboot.basics.recap.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit-Test aus Modul 04: kein Spring-Kontext, das Repository ist ein
 * Mockito-Mock. Geprüft wird nur die Regel im {@link RoomService}.
 */
class RoomServiceTest {

    private final RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
    private final RoomService roomService =
            new RoomService(roomRepository, new RoomProperties("Haus A"));

    @Test
    @DisplayName("Ohne Gebäudeangabe greift das Standardgebäude aus der Konfiguration")
    void fallsBackToDefaultBuilding() {
        when(roomRepository.save(any(Room.class))).thenAnswer(call -> call.getArgument(0));

        Room created = roomService.create("Seminar 2", 16, "  ");

        assertThat(created.getBuilding()).isEqualTo("Haus A");
    }

    @Test
    @DisplayName("Unbekannte ID führt zur RoomNotFoundException")
    void failsForUnknownId() {
        when(roomRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findById(42L))
                .isInstanceOf(RoomNotFoundException.class);
    }
}
