package tech.erben.springboot.basics.recap.task;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST-Schnittstelle der Raumverwaltung mit den Konventionen aus Modul 02.
 * Der Controller übersetzt zwischen HTTP und Fachlogik und enthält selbst
 * keine Regel — deshalb reicht für ihn ein Slice-Test mit gemocktem
 * {@link RoomService}.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * {@code GET /api/rooms} — alle Räume, mit {@code ?minSeats=} nur die
     * mit mindestens dieser Platzzahl.
     */
    @GetMapping
    public List<RoomResponse> list(@RequestParam(required = false) Integer minSeats) {
        List<Room> rooms = (minSeats == null)
                ? roomService.findAll()
                : roomService.findWithAtLeast(minSeats);
        return rooms.stream().map(RoomResponse::from).toList();
    }

    /**
     * {@code GET /api/rooms/{id}} — 200 mit dem Raum. Unbekannte ID → 404
     * über den {@link RestExceptionHandler}.
     */
    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) {
        return RoomResponse.from(roomService.findById(id));
    }

    /** {@code POST /api/rooms} — 201 mit Location-Header auf die neue Ressource. */
    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        Room room = roomService.create(request.name(), request.seats(), request.building());
        return ResponseEntity
                .created(URI.create("/api/rooms/" + room.getId()))
                .body(RoomResponse.from(room));
    }
}
