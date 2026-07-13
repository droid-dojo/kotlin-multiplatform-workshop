# 🧪 Lab 2 - Übung 2.3: Fixing-Task - der Build ist rot!

**Dieser Branch enthält die Musterlösung von Übung 2.2** - plus ein frisches Feature aus dem Team: eine „Stand: 10:15 Uhr"-Anzeige unter dem Wetter. Der Kollege schwört, es funktioniert. Er hat es auf dem Desktop getestet.

```bash
./gradlew :shared:compileKotlinJvm      # BUILD SUCCESSFUL ✅
./gradlew :shared:compileKotlinWasmJs   # BUILD FAILED ❌
```

Willkommen im KMP-Alltag: **Code, der in `commonMain` liegt, aber nicht common ist.** Diese Übung ist bewusst klein - sie trainiert den Reflex, den Sie aus diesem Workshop mitnehmen sollen.

> 📘 Werkzeuge für diese Übung: [Handout, Modul 3: Der expect/actual-Mechanismus](HANDOUT.md#modul-3-der-expectactual-mechanismus) - plus alles, was Sie seit Übung 1.1 gelernt haben.

---

## 🔍 Die Ausgangslage

Schauen Sie ans Ende von `WeatherScreen.kt`:

```kotlin
// Tested on desktop, works fine - ship it!
private fun formatUpdatedAt(isoTime: String): String {
    val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
    ...
}
```

`java.text` existiert auf der JVM (Desktop **und** Android - deshalb fiel es lokal nicht auf), aber nicht auf iOS, nicht im Browser. Der Compiler sagt es deutlich:

```
e: WeatherScreen.kt: Unresolved reference 'java'.
```

Warum kompiliert der Desktop trotzdem? `commonMain` wird **pro Target** mitkompiliert - gegen die Bibliotheken *dieses* Targets. Auf der JVM löst sich `java.text` auf, überall sonst nicht. Genau deshalb prüft eine ehrliche CI immer **alle** Targets (Modul 2.3).

## 🎯 Das Ziel

* Alle Targets kompilieren wieder - `java.*` ist aus `commonMain` verschwunden.
* Die „Stand: …"-Anzeige funktioniert weiterhin, auf jeder Plattform mit der **nativen Datums-API**.

## 🛠 Die Aufgaben im Detail

### Schritt 1: Diagnose

Führen Sie den Wasm-Build aus und lesen Sie den Fehler. Überlegen Sie **vor** dem Coden: Ist das ein Fall für `expect`/`actual` oder für ein Interface (Faustregel, Modul 3.2)? Kleine, zustandslose Blattfunktion → Sie kennen die Antwort.

### Schritt 2: Der Kontrakt

Ersetzen Sie die private Funktion durch eine `expect fun formatUpdatedAt(isoTime: String): String` in einer eigenen Datei im `weather`-Package. Der Compiler listet Ihnen jetzt alle Targets auf, die ein `actual` fordern - Ihre To-do-Liste.

### Schritt 3: Fünf actuals

| Source Set | Native API (Startpunkt) |
| --- | --- |
| `androidMain`, `jvmMain` | `java.time.LocalDateTime` + `DateTimeFormatter` |
| `iosMain` | `platform.Foundation.NSDateFormatter` (`dateFromString`, `stringFromDate`) |
| `jsMain`, `wasmJsMain` | `js("new Date(iso).toLocaleTimeString(...)")` |

Der Zeitstempel kommt von Open-Meteo als `"2026-07-13T10:15"` - lokale Zeit, keine Zone.

### Schritt 4: Der Beweis

```bash
./gradlew :shared:compileKotlinJvm :shared:compileKotlinJs :shared:compileKotlinWasmJs :androidApp:assembleDebug
```

Und einmal Desktop + Web starten: „Stand: … Uhr" muss auf beiden erscheinen.

## ✅ Definition of Done

- [ ] Kein `java.*`-Import mehr in `commonMain` - die Suche nach `java.` in `commonMain` liefert null Treffer.
- [ ] `formatUpdatedAt` ist ein `expect`/`actual`-Paar mit fünf Implementierungen.
- [ ] Alle vier lokal baubaren Targets kompilieren; das iOS-`actual` steht (Review im Plenum).
- [ ] Desktop und Web zeigen die formatierte Uhrzeit.

## 💡 Tipps

* `java.time` statt `SimpleDateFormat` auf JVM/Android - die alte Klasse ist nicht threadsafe; das war schon vor KMP ein Smell.
* In produktivem Code wäre **[kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)** die erste Wahl: eine Multiplatform-Library statt fünf `actual`s. Hier bauen wir die `actual`s bewusst von Hand - Sie sollen einmal alle fünf nativen APIs angefasst haben.
* Android und Desktop teilen sich identischen JVM-Code? Für Neugierige: Ein eigener Zwischen-Source-Set (Modul 2.2) würde das Duplikat eliminieren.

---

**Fertig?** Die Musterlösung finden Sie im Branch `lab-2-final` - dem Endstand des Workshops. 🏁
