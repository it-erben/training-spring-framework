---
name: slide-overflow
description: Prueft Marp-Decks unter slides/ darauf, ob Folieninhalt aus der 1280x720-Buehne laeuft und im PDF abgeschnitten wird, und verkleinert betroffene Folien ueber Dichte-Klassen. Nutze diese Skill, wenn Folien ueberlaufen, abgeschnitten sind, nicht auf die Seite passen, wenn nach einer Textaenderung an slides/ geprueft werden soll, oder wenn nach "Overflow", "passt nicht drauf" oder "abgeschnitten" gefragt wird.
---

# Folienueberlauf pruefen und beheben

Marp rendert jede Folie in eine feste Buehne von 1280x720. Was nicht
hineinpasst, steht im HTML weiter unten, wird im PDF aber abgeschnitten.
Die Pipeline veroeffentlicht PDFs, deshalb ist das PDF der Massstab.

## Pruefen

```bash
node tools/check-slide-overflow.mjs slides/00_Spring_Core/slides.md
node tools/check-slide-overflow.mjs slides/*/slides.md
```

Exit-Code 1, sobald eine Folie ueberlaeuft; `--json` liefert alle Messwerte.
Gemessen wird `scrollHeight` gegen `clientHeight` je `<section>`.

## Beheben

```bash
python3 tools/fix-slide-overflow.py slides/00_Spring_Core/slides.md
python3 tools/fix-slide-overflow.py --dry-run slides/*/slides.md
python3 tools/fix-slide-overflow.py --reset slides/00_Spring_Core/slides.md
```

Setzt eigene Marken zurueck, misst, weist jeder ueberlaufenden Folie die
groesste noch ausreichende Stufe zu und misst nach. Eingefuegt werden nur
ein `<style>`-Block hinter dem Front-Matter und je eine Zeile
`<!-- _class: ... -->` vor dem Folieninhalt. Der Folientext bleibt
unberuehrt. Der Lauf ist idempotent.

Vier Stufen, als Anteil der gemessenen Grundschriftgroesse: `dense` 85%,
`denser` 72%, `densest` 60%, `densest-xs` 53%.

## Grenzen, die eine Entscheidung brauchen

Laeuft eine Folie auch auf der kleinsten Stufe ueber, aendert das Werkzeug
nichts und sieht im Quelltext nach, woran es liegt:

- **Bild auf der Folie.** `font-size` skaliert Grafiken nicht. Die Grafik
  begrenzen oder die Folie teilen.
- **Mehrere Ueberschriften auf einer Folie.** Dann fehlt meist ein
  `---`-Trenner und zwei Folien sind verschmolzen. Trenner einsetzen, nicht
  verkleinern. Ein grosser Ueberlaufwert ist oft genau das.
- **Sonst schlicht zu viel Inhalt.** Aufteilen.

Landet eine Folie auf `densest-xs`, wird das gemeldet. 53% sind lesbar, aber
die Ueberschrift faellt gegenueber Nachbarfolien sichtbar ab. Das ist ein
Hinweis auf zu viel Inhalt: **aufteilen statt weiter verkleinern**, und das
ist eine inhaltliche Entscheidung, keine automatische.

## Fallstricke der Messung

Diese Punkte sind die Gruende fuer den Aufbau der Werkzeuge. Wer daran
etwas aendert, faellt sonst in dieselben Fallen zurueck.

- **Header, Footer und Paginierung** sitzen absolut in der Polsterzone.
  Zaehlt man sie als Inhalt, meldet jede Folie inklusive der Titelfolie
  denselben konstanten Ueberlauf.
- **Codebloecke sind `<marp-pre>`-Custom-Elemente.** Vor dem Upgrade melden
  sie konstant 184px statt ihrer echten Hoehe. Zu frueh gemessen gilt eine
  abgeschnittene Codefolie als passend.
- **Kein `--virtual-time-budget`.** Virtuelle Zeit springt vor, waehrend das
  Upgrade real noch laeuft; Warten wird dadurch wirkungslos und dieselbe
  Datei liefert von Lauf zu Lauf andere Zahlen. Die Steuerung laeuft
  deshalb ueber das DevTools-Protokoll in Echtzeit und pollt, bis die
  Hoehen ueber mehrere Runden stehen.
- **`em` und `%` auf `section` gehen schief.** Die Section erbt im
  Marp-HTML von einem `foreignObject` mit 16px, nicht von der
  Theme-Groesse. `0.85em` ergibt 13.6px statt 24.5px. Die Stufen stehen
  darum absolut in px.
- **Handgeschriebene `<style scoped>`-Bloecke stechen die Klassen aus.**
  Marpit uebersetzt sie in einen Attributselektor mit hoeherer Spezifitaet.
  Findet sich so ein Block auf einer Folie, die trotz Klasse ueberlaeuft:
  Block entfernen und den Lauf wiederholen, damit pro Datei nur ein
  Mechanismus wirkt. Die Advanced-Module nutzen dieses Muster noch.

## Belegen

Zahlen allein reichen nicht, weil eine falsch kalibrierte Messung
widerspruchsfrei aussieht. Nach einer Aenderung an den Werkzeugen gegen die
echte Ausgabe gegenpruefen:

```bash
marp --images png --allow-local-files -o /tmp/s.png slides/00_Spring_Core/slides.md
```

Je eine klar passende und eine klar ueberlaufende Folie ansehen und mit dem
Verdikt vergleichen. Zusaetzlich das Werkzeug zweimal laufen lassen: gleiche
Datei, gleiche Zahlen.

## Voraussetzungen

`marp` im PATH, eine Chrome-Binary und Node. Der Chrome-Pfad kommt aus
`$CHROME_PATH`, sonst aus dem Puppeteer-Cache oder den Standardpfaden.
Keine npm-Abhaengigkeiten.
