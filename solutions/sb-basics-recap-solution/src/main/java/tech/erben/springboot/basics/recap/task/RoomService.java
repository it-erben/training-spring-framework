package tech.erben.springboot.basics.recap.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Fachlogik der Raumverwaltung. Der Service hält die einzige Regel des
 * Modells: Ein Raum ohne Gebäudeangabe landet im Standardgebäude aus
 * {@link RoomProperties}.
 */
@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final RoomProperties properties;

    public RoomService(RoomRepository roomRepository, RoomProperties properties) {
        this.roomRepository = roomRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Room> findWithAtLeast(int seats) {
        return roomRepository.findBySeatsGreaterThanEqual(seats);
    }

    /** Liefert den Raum oder wirft {@link RoomNotFoundException}, die als 404 beim Client ankommt. */
    @Transactional(readOnly = true)
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    @Transactional
    public Room create(String name, int seats, String building) {
        String target = (building == null || building.isBlank())
                ? properties.defaultBuilding()
                : building;
        Room saved = roomRepository.save(new Room(name, seats, target));
        log.info("Raum {} mit {} Plätzen in {} angelegt", saved.getName(), saved.getSeats(), target);
        return saved;
    }
}
