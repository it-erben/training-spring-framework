package tech.erben.springboot.basics.recap.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistenz-Slice aus Modul 04: {@code @DataJpaTest} startet nur JPA und
 * eine In-Memory-Datenbank, kein Web-Layer. Jede Testmethode läuft in einer
 * Transaktion, die danach zurückgerollt wird.
 */
@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @BeforeEach
    void insertRooms() {
        roomRepository.save(new Room("Aula", 120, "Haus A"));
        roomRepository.save(new Room("Seminar 1", 20, "Haus A"));
        roomRepository.save(new Room("Besprechung Nord", 8, "Haus B"));
    }

    @Test
    @DisplayName("findBySeatsGreaterThanEqual filtert nach Mindestplatzzahl")
    void findsRoomsWithEnoughSeats() {
        assertThat(roomRepository.findBySeatsGreaterThanEqual(20))
                .extracting(Room::getName)
                .containsExactlyInAnyOrder("Aula", "Seminar 1");
    }

    @Test
    @DisplayName("findByName findet den Raum über den eindeutigen Namen")
    void findsRoomByName() {
        assertThat(roomRepository.findByName("Aula"))
                .hasValueSatisfying(room -> assertThat(room.getSeats()).isEqualTo(120));
    }
}
