package tech.erben.springboot.basics.core.task;

/**
 * Stellt das {@link TrainerDirectory} als Bean bereit. Die Methode ist
 * fertig geschrieben — dem Container fehlen aber noch zwei Annotationen,
 * damit er sie als Bean-Fabrik erkennt.
 */
// TODO Aufgabe 4: Klasse als Konfigurationsklasse deklarieren
public class TrainerConfig {

    // TODO Aufgabe 4: Methode als Bean-Fabrikmethode deklarieren
    public TrainerDirectory trainerDirectory() {
        TrainerDirectory directory = new TrainerDirectory();
        directory.register(new Trainer("Anna Schmidt", "anna@example.com"));
        directory.register(new Trainer("Ben Weber", "ben@example.com"));
        return directory;
    }
}
