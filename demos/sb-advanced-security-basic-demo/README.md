# Aufgabe 1: Method Security

Füge einen weiteren Benutzer zu den bekannten Benutzern hinzu.
Er kann einen beliebigen Benutzernamen haben. Seine Rolle soll `BACKOFFICE` sein.
Ändere die Methode `findSecretMessage` dahingehend, dass sowohl der bereits vorhandene Administrator
als auch der neue Benutzer mit der Rolle `BACKOFFICE` zugreifen dürfen.

## Hinweis:

- Die Expression die prüft, ob irgendeine Rolle aus einer Liste von Rollen zutrifft, ist `hasAnyRole`.

# Aufgabe 2

In dieser Aufgabe zeige ich euch, wie ihr bestimmte Pfade von der Authentifizierung ausnehmen könnt.

1) Beginnt damit, in `MessageController` eine neue GET-Ressource zu implementieren. Sie kann ein beliebiges,
statisches `String` zurückgeben, z.B. `"Welcome"`. Der Pfad der Ressource sollte `/open` sein,
ist aber beliebig, solange ihr ihn hinterher korrekt in der Konfiguration eintragt.

2) Schaut euch nun die Methode `filterChain` in `SecurityConfiguration` an.
Aktuell ist sie so definiert, dass pauschal alle Requests authentifiziert sein müssen (`anyRequest().authenticated()`).

3) Wenn ihr vor diesem Statement weitere Regeln einfügt, so haben diese den Vorrang, wenn sie greifen.
Fügt direkt vor dem `anyRequest()` folgende Zeile ein:

```java
.requestMatchers("/open").anonymous()
```

Dies bedeutet, dass für den Pfad `/open` alle Requests zugelassen werden, auch die unauthentifizierten.

1) Startet die Anwendung nun neu und testet, ob ihr auch ohne Anmeldung auf `/open` zugreifen könnt.

# Bonus

Spielt nun gerne noch mit dem Request Matching. Erstens könnt ihr mehrere Pattern übergeben, und zweitens
sind auch Wildcards wie `**` erlaubt – alles, was man von AntMatchern aus Spring gewohnt ist.
