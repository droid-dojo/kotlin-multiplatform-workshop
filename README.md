# 🧪 Lab 2 - Übung 2.2: Offline-First - Datenhaltung mit Room

**Dieser Branch enthält die Musterlösung von Übung 2.1.**

Unsere App hat jetzt Architektur - aber kein Gedächtnis: Flugmodus an, App starten → Fehler-Screen. In dieser Übung bekommt sie eine **[Room](https://developer.android.com/kotlin/multiplatform/room)-Datenbank als Single Source of Truth**: Die UI liest nur noch aus der DB, das Netzwerk aktualisiert sie. Einmal geschrieben, läuft das auf Android, iOS und Desktop - und dank einer sauberen Abstraktion bleibt auch das Web funktionsfähig.

> 📘 Die Theorie zu dieser Übung finden Sie im Handout: [Modul 7: Datenhaltung mit Room](HANDOUT.md#modul-7-datenhaltung-mit-room). Die Gradle-Einträge stehen in [Anhang A, Schritt 3 (Übung 2.2): Room 3 einbinden](HANDOUT.md#schritt-3-übung-22-room-3-einbinden).

---

## 🔍 Die Ausgangslage

`WeatherRepository` ist bisher nur ein Durchlauferhitzer:

```kotlin
suspend fun currentWeather(): CurrentWeather =
    api.currentWeather(LATITUDE, LONGITUDE)
```

Kein Netz → Exception → Error-Screen. Eine Enterprise-App zeigt stattdessen den **letzten bekannten Stand**. Und weil wir in KMP sind, lösen wir das einmal - nicht einmal pro Plattform.

## 🎯 Das Ziel

* Eine Room-Datenbank (`Entity`, `DAO`, `Database`) in `commonMain` - mit Kotlin-2.x-Gefühl: `suspend` und `Flow`, keine blockierenden Aufrufe.
* Ein `WeatherCache`-Interface mit **Room-Implementierung** (Android, iOS, Desktop) und **In-Memory-Implementierung** (Web) - die Faustregel aus Modul 3.2 in Aktion.
* Das Repository wird zur Single-Source-of-Truth-Schicht: UI beobachtet die DB, das Netz füllt sie.

## 🛠 Die Aufgaben im Detail

### Schritt 1: Dependencies & Plugins

[HANDOUT.md, Anhang A, Schritt 3 (Übung 2.2): Room 3 einbinden](HANDOUT.md#schritt-3-übung-22-room-3-einbinden): **KSP** und das **Room-Plugin** (`androidx.room3`) einbinden, `room3-runtime` nach `commonMain`, die Treiber (`sqlite-bundled` / `sqlite-web`) in die Plattform-Source-Sets, KSP pro Target registrieren. Sync!

### Schritt 2: Entity & DAO

Im `weather`-Package: eine `WeatherEntity` (`locationKey` als `@PrimaryKey`, dazu Temperatur, Wind, Weather-Code, Zeitstempel) und ein `WeatherDao`:

```kotlin
@Upsert suspend fun upsert(entity: WeatherEntity)
@Query("...") fun observe(key: String): Flow<WeatherEntity?>
```

Plus Mapping-Extensions `WeatherEntity.toDomain()` und `CurrentWeather.toEntity(key)`.

### Schritt 3: Die Database - mit KMP-Dreh

Die `WeatherDatabase` samt `@ConstructedBy` und dem `expect object WeatherDatabaseConstructor` (Vorlage: [Handout, Modul 7.3: Entity, DAO, Database - Fast wie zu Hause](HANDOUT.md#73-entity-dao-database-fast-wie-zu-hause) - die `actual`s generiert der Room-Compiler).

### Schritt 4: Das WeatherCache-Interface

Jetzt die Architektur-Entscheidung dieser Übung: Das Repository soll **nicht** wissen, ob dahinter Room oder etwas anderes steckt.

```kotlin
interface WeatherCache {
    fun observe(): Flow<CurrentWeather?>
    suspend fun store(weather: CurrentWeather)
}

expect fun createWeatherCache(): WeatherCache
```

* `RoomWeatherCache` (in `commonMain`, nutzt das DAO) - zurückgegeben von den `actual`s in `androidMain`, `iosMain`, `jvmMain`. Dort lebt auch der jeweilige `databaseBuilder` (Pfad + `BundledSQLiteDriver`; Android braucht den `Context` - Vorlage im [Handout, Modul 7.3: Entity, DAO, Database - Fast wie zu Hause](HANDOUT.md#73-entity-dao-database-fast-wie-zu-hause)).
* `InMemoryWeatherCache` (ein simpler `MutableStateFlow`) - zurückgegeben von `jsMain`/`wasmJsMain`.

**Android-Detail:** Der `Context` kommt per Init-Funktion aus der App: `initWeatherDatabase(applicationContext)` in `MainActivity.onCreate()`, vor `setContent`.

### Schritt 5: Repository & ViewModel umbauen

* `WeatherRepository`: `fun observeWeather(): Flow<CurrentWeather?>` (aus dem Cache) + `suspend fun refresh()` (API → Cache). Die API wird **nie mehr direkt** an die UI durchgereicht.
* `WeatherViewModel`: sammelt im `init` den Cache-Flow ein (`Success`, sobald Daten da sind) und stößt `refresh()` an. Schlägt der Refresh fehl, obwohl gecachte Daten da sind: Daten **stehen lassen** - der Fehler-Screen ist nur noch für den Kaltstart ohne Cache.

### Schritt 6: Der Offline-Beweis

1. App auf dem Desktop starten, Wetter laden, App **beenden**.
2. Netzwerk kappen, App neu starten → **letzter Stand erscheint**, kein Fehler-Screen.
3. Gleiche Probe auf Android (Flugmodus).

### Bonus: Room im Browser

Room 3 kann auch das Web-Target ([Handout, Modul 7.2: Setup - KSP, Plugin, Treiber](HANDOUT.md#72-setup-ksp-plugin-treiber)): `WebWorkerSQLiteDriver` aus `androidx.sqlite:sqlite-web` persistiert ins Origin Private File System. Wer schnell fertig ist, ersetzt den `InMemoryWeatherCache` - und hat *eine* Datenbank auf fünf Plattformen.

## ✅ Definition of Done

- [ ] Entity, DAO, Database und `RoomWeatherCache` liegen in `commonMain` - kein DB-Code doppelt.
- [ ] Alle DAO-Funktionen sind `suspend` oder liefern `Flow` - Room 3 lässt Ihnen keine Wahl.
- [ ] Das Repository kennt nur noch `WeatherCache` - Room taucht in seiner Signatur nicht auf.
- [ ] Der Offline-Beweis (Schritt 6) gelingt auf Desktop **und** Android.
- [ ] Das Web-Target kompiliert und läuft weiter (In-Memory oder Bonus).
- [ ] `schemas/` enthält nach dem Build das exportierte Schema (v1) - und wandert mit ins Git.

## 💡 Tipps

* Meldet KSP "actual object … not found": Der Room-Compiler ist für dieses Target nicht registriert - `dependencies`-Block gegen Anhang A prüfen.
* Der `databaseBuilder` gehört **nicht** ins Interface - er ist ein Implementierungsdetail der Room-Seite. Das Interface spricht Domain (`CurrentWeather`), nie Entities.
* `@Upsert` statt `@Insert(onConflict = REPLACE)` - kürzer und ohne Lösch-Semantik.
* Der Flow aus `observe()` feuert bei jedem `upsert` neu - genau deshalb braucht das ViewModel nach dem Refresh **keinen** manuellen State-Update mehr.

---

**Fertig?** Die Musterlösung - und damit die Aufgabenstellung für **Übung 2.3 (Fixing-Task)** - finden Sie im Branch `lab-2-uebung-2.3`. Dort erwartet Sie ein Build, den ein Kollege "nur auf dem Desktop getestet" hat …
