# 🧪 Lab 1 — Übung 1.2: Multiplatform Networking mit Ktor

**Dieser Branch enthält die Musterlösung von Übung 1.1** — vergleichen Sie gern mit Ihrer Lösung (`git diff main -- shared/`), bevor Sie hier weitermachen.

Jetzt bekommt unsere Weather App echte Daten: Wir bauen die komplette Netzwerk-Schicht — **einmal, in `commonMain`** — und holen das aktuelle Wetter von der [Open-Meteo API](https://open-meteo.com) (kostenlos, ohne API-Key).

> 📘 Die Theorie zu dieser Übung finden Sie im [HANDOUT.md](HANDOUT.md), **Modul 4** (Ktor & kotlinx.serialization). Die fertigen Dependency-Einträge stehen in **Anhang A, Schritt 1 + 2**.

---

## 🔍 Die Ausgangslage

Die App kennt seit Übung 1.1 ihre Plattform und ihre Zeitzone — aber Wetterdaten hat sie noch keine. Der Request, den wir bauen (probieren Sie ihn im Browser aus!):

```
https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,weather_code,wind_speed_10m&timezone=Europe/Berlin
```

```json
{
  "latitude": 52.52, "longitude": 13.41,
  "current": {
    "time": "2026-07-13T10:15",
    "temperature_2m": 21.4,
    "weather_code": 3,
    "wind_speed_10m": 12.7
  }
}
```

Retrofit und Gson helfen uns hier nicht — beide sind JVM-only (Handout, Modul 4.1). Unsere Werkzeuge: **Ktor** und **kotlinx.serialization**.

## 🎯 Das Ziel

* Eine `WeatherApi` in `commonMain`, die das aktuelle Wetter als **typsicheres Domain-Modell** liefert.
* Pro Target die **richtige Engine**: OkHttp (Android, Desktop), Darwin (iOS), Js (Web).
* Die App zeigt auf Desktop, Android und im Web die aktuelle Temperatur in Berlin.

## 🛠 Die Aufgaben im Detail

### Schritt 1: Dependencies einbinden

Ergänzen Sie `gradle/libs.versions.toml` und `shared/build.gradle.kts` um das **Serialization-Plugin**, **Ktor** (Core, ContentNegotiation, kotlinx-json, Logging) und die **Engines pro Source Set** — die fertigen Einträge stehen im [HANDOUT.md, Anhang A](HANDOUT.md#anhang-a-setup--dependencies-für-die-übungen). Danach: Sync!

### Schritt 2: Die DTOs

Legen Sie in `commonMain` das Package `weather` an und modellieren Sie die JSON-Antwort als `@Serializable` data classes (`ForecastDto`, `CurrentDto`). Die Snake-Case-Felder (`temperature_2m`, …) bekommen per `@SerialName` idiomatische Kotlin-Namen.

### Schritt 3: Das Domain-Modell

DTOs bleiben in der Datenschicht! Erstellen Sie ein Domain-Modell `CurrentWeather` (Temperatur, Wind, WMO-Weather-Code, Zeitstempel) und eine Mapping-Extension `fun ForecastDto.toDomain(): CurrentWeather`.

### Schritt 4: Die WeatherApi

Bauen Sie eine Klasse `WeatherApi` mit einem konfigurierten `HttpClient` (ContentNegotiation mit `ignoreUnknownKeys = true`, Logging) und:

```kotlin
suspend fun currentWeather(latitude: Double, longitude: Double): CurrentWeather
```

**Der Clou:** Übergeben Sie als `timezone`-Parameter die `timeZoneId` aus Übung 1.1 — Ihr `expect`/`actual`-Code zahlt sich schon aus.

### Schritt 5: Anzeigen!

Ersetzen Sie in `App.kt` den Greeting-Block: Beim Aufklappen lädt ein `LaunchedEffect` das Wetter und zeigt die Temperatur an. Ein `try`/`catch` mit Fehlertext genügt fürs Erste — sauberes State-Management (`Loading`/`Success`/`Error`) bauen wir in Übung 2.1.

**Android-Falle:** Ohne `<uses-permission android:name="android.permission.INTERNET" />` im `AndroidManifest.xml` sieht Android keinen Netzwerk-Request — der Wizard legt die Permission nicht an.

### Schritt 6: Der Mapping-Test

Schreiben Sie in `commonTest` einen Test, der ein Beispiel-JSON mit `Json.decodeFromString` parst und das Mapping nach `CurrentWeather` prüft. Er läuft mit `./gradlew :shared:jvmTest` — und würde auf **jedem** Target dasselbe prüfen.

## ✅ Definition of Done

- [ ] `WeatherApi`, DTOs und Domain-Modell liegen in `commonMain` — kein plattformspezifischer Netzwerk-Code.
- [ ] Jedes Target hat genau eine Engine-Dependency (Anhang A) — und `commonMain` kennt keine davon.
- [ ] Desktop, Android und Web zeigen die aktuelle Temperatur in Berlin.
- [ ] Der `timezone`-Parameter kommt aus `Platform.timeZoneId` (Übung 1.1).
- [ ] Der Mapping-Test ist grün: `./gradlew :shared:jvmTest`.

## 💡 Tipps

* Startet der Client nicht ("Failed to find HTTP client engine"), fehlt die Engine im **jeweiligen** Source Set — prüfen Sie `shared/build.gradle.kts` gegen Anhang A.
* `ignoreUnknownKeys = true` nicht vergessen — Open-Meteo liefert mehr Felder, als unsere DTOs kennen (`current_units` etc.).
* Das Logging-Plugin zeigt Request und Response in der Konsole — auf dem Desktop das schnellste Debugging.
* Im Web gilt CORS: Open-Meteo erlaubt Browser-Requests explizit — bei eigenen APIs wäre das Ihre erste Fehlerquelle.

---

**Fertig?** Die Musterlösung finden Sie im Branch `lab-1-final` — damit ist Tag 1 geschafft. 🎉 Tag 2 startet im Branch `lab-2-uebung-2.1`.
