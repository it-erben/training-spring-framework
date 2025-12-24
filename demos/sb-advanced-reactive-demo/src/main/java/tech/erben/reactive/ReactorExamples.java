package tech.erben.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactorExamples {

    public static void main(String[] args) {
        // Beispiel 1: Erstellung eines Flux und Abo
        Flux<String> fruitsFlux = Flux.just(
            "Apfel",
            "Birne",
            "Banane",
            "Orange",
            "Traube"
        );
        fruitsFlux.subscribe(System.out::println);

        // Beispiel 2: Erstellung eines Mono und Abo
        Mono<String> singleFruit = Mono.just("Erdbeere");
        singleFruit.subscribe(System.out::println);

        // Beispiel 3: Verwendung von Flux und filter()
        Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.filter(number -> number % 2 == 0).subscribe(System.out::println);

        // Beispiel 4: Verwendung von Flux und flatMap()
        Flux<List<Integer>> fluxOfLists = Flux.just(
            List.of(1, 2, 3),
            List.of(4, 5, 6),
            List.of(7, 8, 9)
        );

        Flux<Integer> flattenedFlux = fluxOfLists.flatMap(Flux::fromIterable);
        flattenedFlux.subscribe(System.out::println);

        // Beispiel 5: Umgang mit Fehlern in Flux
        Flux<String> errorFlux = Flux.concat(
            Flux.just("Normalwert"),
            Flux.error(new RuntimeException("Beispiel Fehler"))
        );
        errorFlux
            .onErrorResume(e -> Flux.just("Fehler aufgetreten, aber aufgefangen."))
            .subscribe(System.out::println);

        // Beispiel 6: Umgang mit Fehlern in Mono
        Mono<String> errorMono = Mono.error(new RuntimeException("Beispiel Mono Fehler"));
        errorMono
            .onErrorResume(e -> Mono.just("Fehler aufgetreten, aber aufgefangen (Mono)."))
            .subscribe(System.out::println);
    }
}
