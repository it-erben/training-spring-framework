package tech.erben.springboot.basics.web.task;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Loesung Aufgaben 1-3: REST-Schnittstelle der Kursverwaltung. Der
 * Controller uebersetzt nur zwischen HTTP und Fachlogik — die eigentliche
 * Arbeit macht der {@link CourseService}.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** Aufgabe 1: {@code GET /api/courses} — 200 mit allen Kursen inklusive Bruttogebuehr. */
    @GetMapping
    public List<CourseResponse> list() {
        return courseService.findAll().stream()
                .map(CourseResponse::from)
                .toList();
    }

    /**
     * Aufgabe 2: {@code GET /api/courses/{code}} — 200 mit dem Kurs. Bei
     * unbekanntem Code wirft der Service eine {@link CourseNotFoundException},
     * die der {@link RestExceptionHandler} in 404 uebersetzt.
     */
    @GetMapping("/{code}")
    public CourseResponse get(@PathVariable String code) {
        return CourseResponse.from(courseService.findByCode(code));
    }

    /**
     * Aufgabe 3: {@code POST /api/courses} — 201 mit Location-Header auf die
     * neue Ressource. {@code @Valid} loest die Bean-Validation-Pruefung des
     * {@link CourseRequest} aus (Aufgabe 4); schlaegt sie fehl, kommt es gar
     * nicht erst bis in diese Methode (400).
     */
    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        Course course = courseService.create(request.toCourse());
        return ResponseEntity
                .created(URI.create("/api/courses/" + course.code()))
                .body(CourseResponse.from(course));
    }

    /**
     * Aufgabe 3: {@code DELETE /api/courses/{code}} — 204 ohne Body. Bei
     * unbekanntem Code wieder 404 ueber den Exception-Handler.
     */
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        courseService.delete(code);
    }

    /**
     * Bonusaufgabe: {@code PUT /api/courses/{code}} — 200 mit dem
     * aktualisierten Kurs. Der Code aus dem Pfad gewinnt gegenueber dem
     * Body; unbekannter Code → 404 ueber den Exception-Handler.
     */
    @PutMapping("/{code}")
    public CourseResponse update(@PathVariable String code,
                                 @Valid @RequestBody CourseRequest request) {
        Course course = courseService.update(code,
                new Course(code, request.title(), request.seats(), request.netFee()));
        return CourseResponse.from(course);
    }
}
