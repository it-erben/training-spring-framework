package tech.erben.springboot.basics.testing.task;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST-Schnittstelle der Anmeldung mit den Konventionen aus Modul 02. Der
 * Controller übersetzt nur zwischen HTTP und Fachlogik. Deshalb
 * lässt er sich im Slice-Test isoliert prüfen, indem der
 * {@link ParticipantService} durch ein Mock ersetzt wird.
 */
@RestController
@RequestMapping("/api/courses")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * {@code POST /api/courses/{code}/participants} — 201 mit
     * Location-Header, wenn die Anmeldung geklappt hat, 409 (Conflict),
     * wenn der Kurs voll ist. Unbekannter Code → 404 über den
     * {@link RestExceptionHandler}.
     */
    @PostMapping("/{code}/participants")
    public ResponseEntity<Void> register(@PathVariable String code,
                                         @RequestBody RegistrationRequest request) {
        boolean registered = participantService.register(code, request.email());
        if (!registered) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity
                .created(URI.create("/api/courses/" + code + "/participants"))
                .build();
    }
}
