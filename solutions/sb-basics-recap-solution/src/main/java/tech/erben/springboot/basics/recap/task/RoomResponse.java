package tech.erben.springboot.basics.recap.task;

/**
 * Ausgabeformat der Raum-Endpunkte. Die Entity bleibt hinter dem Service:
 * Was nach außen geht, entscheidet dieser Record, nicht das
 * Datenbankmodell.
 */
public record RoomResponse(Long id, String name, int seats, String building) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getSeats(), room.getBuilding());
    }
}
