# 🧪 Lab 1 — Übung 1.1: Hello, alle Plattformen

**Willkommen zum Kotlin-Multiplatform-Workshop!**

Ausgangspunkt ist ein frisches KMP-Projekt mit fünf Targets: **Android, iOS, Desktop, Web (JS) und Web (Wasm)** — erzeugt mit dem offiziellen [KMP-Wizard](https://kmp.jetbrains.com/?android=true&ios=true&iosui=compose&includeTests=true) von JetBrains. Es läuft — aber es weiß noch fast nichts über die Plattformen, auf denen es lebt. Das ändern wir jetzt: In dieser Übung lernen Sie das Projekt kennen und schreiben Ihren ersten eigenen `expect`/`actual`-Code, der auf **allen fünf Targets** funktioniert.

> 📘 Die Theorie zu dieser Übung finden Sie im [HANDOUT.md](HANDOUT.md), **Modul 2** (Build-System & Source Sets) und **Modul 3** (`expect`/`actual`).

---

## 🔍 Die Ausgangslage

Werfen Sie einen Blick in `shared/src/commonMain/.../Platform.kt`:

```kotlin
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
```

Ein Interface, eine `expect`-Funktion — und in `androidMain`, `iosMain`, `jvmMain`, `jsMain` und `wasmJsMain` jeweils ein `actual`, das den Plattform-Namen liefert. Die `App()`-Composable in `commonMain` zeigt ihn nach einem Button-Klick an.

Unsere Weather App wird später die **Zeitzone des Geräts** brauchen (die [Open-Meteo](https://open-meteo.com) API liefert Zeitstempel pro Zeitzone). Eine Zeitzone auslesen kann jede Plattform — aber jede anders: `android.icu.util.TimeZone` auf Android, `java.util.TimeZone` auf der JVM, `NSTimeZone` auf iOS, `Intl` im Browser. Ein Fall wie aus dem Lehrbuch für `expect`/`actual`.

## 🎯 Das Ziel

* Sie haben die App auf **Desktop (mit Hot Reload!), Android und im Web** gestartet.
* Das `Platform`-Interface hat eine neue Property `timeZoneId` — und **alle fünf Targets** liefern sie über ihre native API.
* Die UI zeigt Plattform-Name **und** Zeitzone an.

## 🛠 Die Aufgaben im Detail

### Schritt 1: Projekt-Tour

Beantworten Sie für sich (Handout, Modul 2 hilft):

1. In welcher Datei werden die fünf **Targets** deklariert — und welches Artefakt erzeugt jedes davon?
2. Warum liegt `App.kt` in `commonMain`, `MainViewController.kt` aber in `iosMain`?
3. Welche Source Sets **sehen** den Code aus `commonMain` — und warum gilt das umgekehrt nicht?

### Schritt 2: Die App starten

```bash
# Desktop — unser Standard-Workflow: Hot Reload, Änderungen erscheinen live
./gradlew :desktopApp:hotRun --auto

# Web (Wasm) — öffnet einen Dev-Server im Browser
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Android — wie gewohnt über die IDE (Run-Konfiguration "androidApp")
```

Lassen Sie den Desktop-Prozess für den Rest der Übung laufen — jede Änderung im Shared Code erscheint sofort.

### Schritt 3: Das Interface erweitern

Ergänzen Sie in `commonMain` das `Platform`-Interface:

```kotlin
interface Platform {
    val name: String
    val timeZoneId: String
}
```

Beobachten Sie, was passiert: **Der Compiler zwingt Sie nun in jedes einzelne Target.** Genau so fühlt sich der `expect`/`actual`-Kontrakt im Alltag an — vergessene Plattformen sind ein Compile-Fehler, kein Laufzeit-Crash.

### Schritt 4: Fünf actuals, fünf native APIs

Implementieren Sie `timeZoneId` in allen Plattform-Klassen:

| Source Set | Native API (Startpunkt) |
| --- | --- |
| `androidMain` | `android.icu.util.TimeZone.getDefault()` — die ICU-API des Android-Frameworks (ab API 24) |
| `jvmMain` | `java.util.TimeZone.getDefault()` |
| `iosMain` | `platform.Foundation.NSTimeZone` → `localTimeZone` |
| `jsMain` | `js("Intl.DateTimeFormat().resolvedOptions().timeZone")` |
| `wasmJsMain` | wie `jsMain` — aber: `js(...)` muss hier der **einzige Ausdruck** einer Funktion sein |

### Schritt 5: Sichtbar machen

Erweitern Sie `App.kt` in `commonMain`, sodass unter dem Greeting auch die Zeitzone erscheint — z.B. `Text("Zeitzone: ${getPlatform().timeZoneId}")`. Speichern — der Desktop aktualisiert sich von selbst. 🎉

### Schritt 6: Der Kompilier-Beweis

```bash
./gradlew :shared:compileKotlinJvm :shared:compileKotlinJs :shared:compileKotlinWasmJs :androidApp:assembleDebug
```

## ✅ Definition of Done

- [ ] Die drei Fragen der Projekt-Tour können Sie beantworten (Stichprobe im Plenum!).
- [ ] `Platform.timeZoneId` existiert in `commonMain` — und alle **fünf** `actual`-Implementierungen nutzen die jeweilige native API.
- [ ] Desktop, Android und Web zeigen Name **und** Zeitzone der Plattform an.
- [ ] Der Kompilier-Beweis aus Schritt 6 läuft ohne Fehler durch.
- [ ] Das iOS-`actual` ist geschrieben (Code-Review im Plenum — kompiliert wird es nur von Teilnehmenden mit Mac).

## 💡 Tipps

* In `iosMain` tippen Sie einfach `NSTimeZone.` und lassen die Autocomplete die Foundation-API zeigen — die kompletten Apple-SDKs sind als Kotlin-Deklarationen da (Handout, Modul 3.3).
* Auf dem JS-Target liefert `js(...)` ein `dynamic` — deklarieren Sie den Rückgabetyp der Funktion explizit als `String`.
* Wenn der Web-Build meckert, aber der Desktop läuft: Sie haben vermutlich ein `actual` vergessen — die Fehlermeldung nennt Target und erwartete Signatur.
* Linux/Windows überspringen die iOS-Targets automatisch (Warnung im Log ist okay) — das ist das erwartete Verhalten aus Handout, Modul 2.3.
* Auf der JVM würde statt `java.util.TimeZone` auch [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) funktionieren (`TimeZone.currentSystemDefault().id`) — die Bibliothek läuft sogar in `commonMain`. Hier greifen wir bewusst pro Plattform auf die native API zu, um den `expect`/`actual`-Kontrakt zu üben.

---

**Fertig?** Die Musterlösung — und damit die Aufgabenstellung für **Übung 1.2 (Multiplatform Networking mit Ktor)** — finden Sie im Branch `lab-1-uebung-1.2`.
