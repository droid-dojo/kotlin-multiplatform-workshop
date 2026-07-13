# 🧪 Lab 2 — Übung 2.1: Shared ViewModel & Compose UI

**Willkommen zu Tag 2!** Dieser Branch entspricht dem Endstand von Tag 1 (`lab-1-final`).

Unsere Netzwerk-Schicht funktioniert — aber die UI ist noch Quick & Dirty: ein `LaunchedEffect` mit `try`/`catch` direkt in der Composable. Heute bauen wir daraus eine echte Architektur: **ein [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) mit StateFlow, geteilt über alle Plattformen** — plus eine Compose-UI, die jeden Zustand sauber behandelt.

> 📘 Die Theorie zu dieser Übung finden Sie im [HANDOUT.md](HANDOUT.md), **Modul 5** (Coroutines, Flow & ViewModels) und **Modul 6** (UI-Integration).

---

## 🔍 Die Ausgangslage

Werfen Sie einen Blick in `App.kt`:

```kotlin
LaunchedEffect(Unit) {
    weatherText = try { ... } catch (e: Exception) { "Fehler: ${e.message}" }
}
```

Drei Probleme: Der State lebt in der UI (überlebt keine Recomposition-Grenzen), Laden/Fehler/Erfolg sind nur Strings statt Typen, und testbar ist hier nichts. Auf Android würden Sie jetzt ein ViewModel schreiben — **genau das tun wir, nur eben einmal für alle Plattformen.**

## 🎯 Das Ziel

* Ein `WeatherViewModel : ViewModel()` in `commonMain` mit `StateFlow<WeatherUiState>`.
* Ein `sealed interface WeatherUiState` — der Compiler erzwingt die Behandlung aller Zustände.
* Ein `WeatherScreen`, der auf Android, Desktop und im Web identisch läuft.

## 🛠 Die Aufgaben im Detail

### Schritt 1: Der UI-Zustand als Typ

Modellieren Sie im `weather`-Package:

```kotlin
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: CurrentWeather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
```

### Schritt 2: Das Repository

Ziehen Sie eine dünne Schicht zwischen API und ViewModel ein: `class WeatherRepository(private val api: WeatherApi)` mit `suspend fun currentWeather(): CurrentWeather` (Berlin-Koordinaten wandern hierhin). In Übung 2.2 wächst diese Klasse zur Offline-First-Schicht — die UI wird davon nichts merken.

### Schritt 3: Das ViewModel

`class WeatherViewModel(private val repository: WeatherRepository) : ViewModel()` — mit privatem `MutableStateFlow`, öffentlichem `StateFlow` und einer `refresh()`-Funktion, die in `viewModelScope.launch { ... }` lädt (Vorlage: Handout, Modul 5.4). Erster Refresh im `init`-Block.

### Schritt 4: Der WeatherScreen

Neue Datei `WeatherScreen.kt` in `commonMain`:

* ViewModel via `viewModel { WeatherViewModel(...) }` (die Factory-Lambda-Variante — die Library ist schon eingebunden).
* State einsammeln mit `collectAsStateWithLifecycle()`.
* Ein `when` über den drei Zuständen: `CircularProgressIndicator`, Fehlertext mit **Retry-Button**, Wetteranzeige mit **Aktualisieren-Button**.

`App()` schrumpft auf `MaterialTheme { WeatherScreen() }` — Greeting, Button und Logo haben ausgedient.

### Schritt 5: Auf alle Plattformen

Starten Sie Desktop (`hotRun --auto`), Android und Web. **Testen Sie den Error-Zustand wirklich:** Netzwerk kappen (Flugmodus / WLAN aus), App starten → Fehlermeldung mit Retry → Netz an → Retry → Daten. Kein Neustart nötig.

### Bonus: Der WMO-Code wird lesbar

`weatherCode` ist bisher eine nackte Zahl. Schreiben Sie eine Übersetzung (WMO-Codes: 0 = klar, 1–3 = Wolken, 45/48 = Nebel, 51–67 = Regen, 71–77 = Schnee, 80–82 = Schauer, 95+ = Gewitter) — mit Emoji wird's hübsch: ☀️ 🌤 🌫 🌧 ❄️ ⛈

## ✅ Definition of Done

- [ ] `App()` enthält keinen `LaunchedEffect` und kein `try`/`catch` mehr — nur Theme + `WeatherScreen`.
- [ ] `WeatherViewModel` und `WeatherUiState` liegen in `commonMain` — null plattformspezifischer Code.
- [ ] Das `when` im Screen ist **exhaustiv** (kein `else`-Zweig!).
- [ ] Der Flugmodus-Test funktioniert: Error-State → Retry → Success, ohne App-Neustart.
- [ ] Desktop, Android und Web zeigen dieselbe UI.

## 💡 Tipps

* `viewModel { ... }` kommt aus `androidx.lifecycle.viewmodel.compose` — die KMP-Variante steckt seit Projektbeginn in den Dependencies.
* `collectAsStateWithLifecycle()` statt `collectAsState()` — auf Android pausiert das Sammeln im Hintergrund, auf den anderen Targets verhält es sich identisch.
* Bauen Sie die drei Zustände zuerst auf dem Desktop mit Hot Reload — mit einem hartkodierten `WeatherUiState.Error("Test")` sehen Sie jeden Zweig sofort.
* Fehlt im `when` ein Zustand, sagt es Ihnen der Compiler — genau dafür haben wir das `sealed interface`.

---

**Fertig?** Die Musterlösung — und damit die Aufgabenstellung für **Übung 2.2 (Offline-First mit Room)** — finden Sie im Branch `lab-2-uebung-2.2`.
