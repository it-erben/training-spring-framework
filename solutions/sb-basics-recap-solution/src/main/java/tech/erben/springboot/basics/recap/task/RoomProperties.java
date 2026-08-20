package tech.erben.springboot.basics.recap.task;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfiguration der Raumverwaltung, gebunden an den Präfix
 * {@code roomadmin} in {@code application.properties}. Als Record ist die
 * Bindung an den Konstruktor gebunden und der Wert danach unveränderlich.
 *
 * @param defaultBuilding Gebäude, das ein Raum ohne eigene Angabe bekommt
 */
@ConfigurationProperties("roomadmin")
public record RoomProperties(String defaultBuilding) {
}
