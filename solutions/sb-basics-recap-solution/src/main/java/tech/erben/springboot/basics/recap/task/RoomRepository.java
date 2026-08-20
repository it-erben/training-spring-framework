package tech.erben.springboot.basics.recap.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Raum-Zugriff aus Modul 03. Beide Methoden sind Derived Queries: Spring
 * Data leitet das SQL aus dem Methodennamen ab, eine Implementierung gibt
 * es nirgends.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findBySeatsGreaterThanEqual(int seats);

    Optional<Room> findByName(String name);
}
