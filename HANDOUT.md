# Workshop: Kotlin Multiplatform — Eine Codebasis, alle Plattformen

## Einführung

Willkommen zum Kotlin-Multiplatform-Workshop! In den nächsten zwei Tagen bauen wir gemeinsam **"The Shared App"** — eine Weather App, deren komplette Logik (Networking, Datenhaltung, State-Management) **einmal** in Kotlin geschrieben wird und auf **Android, iOS, Desktop und im Web** läuft.

Die Leitfrage des Workshops: **Was teilen wir — und was nicht?** KMP ist kein Alles-oder-Nichts-Framework, sondern ein Baukasten. Wer die Mechanik dahinter versteht (Targets, Source Sets, `expect`/`actual`), kann für jedes Projekt selbst entscheiden, wo Code-Sharing Gewinn bringt und wo native Arbeit die bessere Wahl ist.

### Drei Hinweise vorab

1. **Praxis in Compose Multiplatform:** Alle Übungen laufen auf **Android, Desktop und im Web** — dort haben wir die schnellsten Feedback-Schleifen (Desktop mit Hot Reload!). Der **iOS-/SwiftUI-Teil wird als Code-Showcase behandelt**: Wir schreiben den iOS-Code mit und besprechen ihn im Detail, kompilieren ihn aber nicht live — dafür wäre Mac-Hardware nötig. Wer ein MacBook dabei hat, kann selbstverständlich mitbauen.
2. **Kotlin-Vorkenntnisse:** Der Workshop setzt Grundkenntnisse in Kotlin voraus (`val`/`var`, `data class`, Null Safety, Lambdas). Falls die Runde hier unterschiedlich aufgestellt ist, schieben wir zu Beginn ein kompaktes Kotlin-Modul ein — der Spickzettel dazu steht in **Anhang B**. In dem Fall straffen wir dafür an anderer Stelle.
3. **Arbeitsweise:** Die Aufgabenstellung der aktuellen Übung steht immer im `README.md` des Branches, auf dem Sie gerade arbeiten. Der jeweils nächste Branch enthält die Musterlösung — und gleichzeitig die Aufgabenstellung für die nächste Übung.

### Die Agenda für Tag 1

| Block | Thema |
| --- | --- |
| Theorie | Die KMP-Philosophie und Architektur (Modul 1) |
| Theorie | Build-System und Gradle Multiplatform Plugin (Modul 2) |
| Theorie | Der `expect`/`actual`-Mechanismus & Interoperabilität (Modul 3) |
| Theorie | Multiplatform Networking mit Ktor (Modul 4) |
| **Praxis** | **Übung 1.1:** Hello, alle Plattformen — Projekt-Tour & `expect`/`actual` |
| **Praxis** | **Übung 1.2:** Multiplatform Networking — die Weather API mit Ktor |

### Die Agenda für Tag 2

| Block | Thema |
| --- | --- |
| Theorie | Shared Logic in der Praxis: Coroutines, Flow & ViewModels (Modul 5) |
| Theorie | UI-Integration: Compose Multiplatform & SwiftUI-Showcase (Modul 6) |
| Theorie | Datenhaltung mit Room (Modul 7) |
| Theorie | Web-Targets: Kotlin/JS und Kotlin/Wasm (Modul 8) |
| Theorie | Final Roadmap: Migrationspfad zu KMP (Modul 9) |
| **Praxis** | **Übung 2.1:** Shared ViewModel & Compose UI auf allen Targets |
| **Praxis** | **Übung 2.2:** Offline-First — Datenhaltung mit Room |
| **Praxis** | **Übung 2.3:** Fixing-Task — plattformspezifische Fehler mit `expect`/`actual` beheben |

---

# Tag 1: Philosophie, Build-System, expect/actual & Networking

## Modul 1: Die KMP-Philosophie und Architektur

### 1.1 Das Code-Sharing-Spektrum: Was teilen wir eigentlich?

Cross-Platform ist keine Ja/Nein-Frage, sondern ein Regler. Die drei klassischen Positionen:

| Strategie | Geteilt wird … | Typische Vertreter |
| --- | --- | --- |
| **Share nothing** | Nichts — zwei native Apps | Klassische Android-/iOS-Teams |
| **Share logic** | Models, Networking, DB, Business-Logik | **KMP (der Kern-Use-Case)** |
| **Share everything** | Logik **und** UI | Flutter, React Native, **Compose Multiplatform** |

**Warum Logik-Sharing oft besser ist als UI-Sharing:**

* **Die Logik ist der teure Teil.** Bugs leben selten im Button, sondern in Parsing, Caching, State-Übergängen. Genau dieser Code ist auf allen Plattformen *identisch* — ihn zweimal zu schreiben heißt, ihn zweimal zu debuggen und doppelt zu testen.
* **Die UI ist der plattformspezifische Teil.** Navigation, Gesten, Accessibility, Look & Feel — hier *wollen* Nutzer Plattform-Konventionen. Eine geteilte UI muss diese Unterschiede entweder emulieren oder ignorieren.
* **Risikominimierung:** Wer nur Logik teilt, hat jederzeit einen Exit. Das Shared Module ist aus Sicht der iOS-App "nur eine Library" — im Worst Case ersetzt man sie durch Swift-Code, die App selbst bleibt unberührt.

> **Faustregel:**
> KMP zwingt nicht zu einer Strategie — das ist der Kernunterschied zu Flutter & Co. Man startet mit geteilten Models, nimmt Networking dazu, später die ViewModels, und entscheidet **pro Schicht**, ob sich Sharing lohnt. Mit Compose Multiplatform lässt sich der Regler heute bis zur UI aufdrehen — muss man aber nicht.

### 1.2 Wie kommt Kotlin auf jede Plattform? Kein Trick, drei Compiler

KMP funktioniert **ohne eigene Runtime, ohne Bridge, ohne eingebettete VM**. Derselbe Kotlin-Code wird von unterschiedlichen Compiler-Backends in das jeweils *native* Format übersetzt:

```
                        ┌──────────────┐
                        │  Kotlin Code │
                        └──────┬───────┘
          ┌────────────────────┼────────────────────┐
   Kotlin/JVM             Kotlin/Native         Kotlin/JS & /Wasm
          │                    │                    │
   JVM-Bytecode          LLVM-Maschinencode     JavaScript /
   (Android, Desktop)    (iOS, macOS, …)        WebAssembly
```

Das unterscheidet KMP fundamental von den Alternativen:

| | KMP | Flutter | React Native |
| --- | --- | --- | --- |
| Sprache | Kotlin | Dart | JS/TS |
| Läuft auf iOS als | **Nativer LLVM-Code** | Dart-Runtime (AOT) | JS-Engine + Bridge/JSI |
| UI | Nativ **oder** Compose | Eigenes Rendering (Impeller) | Native Views via Bridge |
| Adoption inkrementell? | **Ja — pro Schicht** | Schwer (App-Rewrite) | Teilweise |
| Interop mit Platt­form-APIs | Direkt (kein Wrapper) | Über Platform Channels | Über Native Modules |

Für iOS heißt das konkret: Das Shared Module wird zu einem ganz normalen **Framework** kompiliert, das Xcode einbindet wie jede Swift-Library. Der iOS-Entwickler sieht Objective-C/Swift-APIs — dass dahinter Kotlin steckt, ist ein Implementierungsdetail (mehr dazu in Modul 3).

### 1.3 Projektstruktur: Shared Module und Plattform-Targets

So ist unser Workshop-Projekt aufgebaut — es ist der Standard-Schnitt für KMP-Projekte:

```
kmp-workshop/
├── shared/          ← das Herzstück: gemeinsamer Code (+ Compose-UI)
│   └── src/
│       ├── commonMain/    ← Code für ALLE Plattformen
│       ├── androidMain/   ← nur Android (voller Zugriff aufs Android SDK)
│       ├── iosMain/       ← nur iOS (voller Zugriff auf UIKit, Foundation, …)
│       ├── jvmMain/       ← nur Desktop-JVM
│       ├── jsMain/        ← nur Kotlin/JS
│       ├── wasmJsMain/    ← nur Kotlin/Wasm
│       └── commonTest/    ← gemeinsame Tests (laufen auf JEDEM Target!)
├── androidApp/      ← dünner Android-Einstiegspunkt (Activity)
├── iosApp/          ← dünner iOS-Einstiegspunkt (Xcode-Projekt, SwiftUI)
├── desktopApp/      ← dünner Desktop-Einstiegspunkt (main-Funktion)
└── webApp/          ← dünner Web-Einstiegspunkt (JS + Wasm)
```

Zwei Beobachtungen, die das Modell erklären:

1. **Die App-Module sind bewusst dumm.** `MainActivity`, `iOSApp.swift`, `main.kt` — jeder Einstiegspunkt ist nur wenige Zeilen lang und delegiert sofort ins Shared Module. Je dünner die Plattform-Schicht, desto mehr Code ist geteilt.
2. **`commonMain` ist die Schnittmenge, nicht die Obermenge.** In `commonMain` steht nur zur Verfügung, was *überall* existiert: die Kotlin-Stdlib und Multiplatform-Libraries. Kein `java.io.File`, kein `android.content.Context`, kein `UIKit`. Wer Plattform-APIs braucht, geht über `expect`/`actual` (Modul 3) in die plattformspezifischen Source Sets.

### 1.4 Das Ökosystem: Wo stehen Google und JetBrains?

KMP ist dem Experimentierstadium lange entwachsen — die beiden relevanten Player haben sich klar positioniert:

**JetBrains** (erfinden Kotlin und treiben KMP):
* **Kotlin Multiplatform ist stabil**, Kotlin/Wasm auf der Zielgeraden.
* **Compose Multiplatform:** iOS-Support ist **seit Mai 2025 stabil** (CMP 1.8), Desktop stabil, Web (Wasm) im Beta-Stadium und in schneller Entwicklung. Seit CMP 1.10 ist **Compose Hot Reload** stabil und standardmäßig aktiv — davon profitieren wir in jeder Übung.
* Die Kern-Libraries — **Ktor, kotlinx.coroutines, kotlinx.serialization, kotlinx-datetime** — sind von Haus aus multiplattform.

**Google** (offizieller KMP-Support seit I/O 2024):
* Google unterstützt KMP offiziell für das **Teilen von Business-Logik zwischen Android und iOS** — und nutzt es selbst (u.a. Google Workspace).
* Die wichtigsten **Jetpack-Libraries sind KMP-fähig**: Room, DataStore, ViewModel, Lifecycle, Paging — genau der Stack, den Android-Teams ohnehin kennen. Für uns heißt das: Wissen aus der Android-Welt (Room!) nehmen wir 1:1 mit auf die anderen Plattformen.
* Das **Android Gradle Plugin bringt ein eigenes KMP-Library-Plugin** mit (`com.android.kotlin.multiplatform.library`) — Android ist in KMP-Projekten ein Target erster Klasse.

Dazu kommt ein wachsendes Community-Ökosystem — durchsuchbar auf **[klibs.io](https://klibs.io)**, dem JetBrains-Suchportal für KMP-Libraries. Produktiv im Einsatz ist KMP u.a. bei McDonald's, Netflix, Forbes, Bloomberg und Shopify.

> **Faustregel:**
> Die Frage "Ist KMP schon reif?" ist 2026 falsch gestellt. Die richtige Frage lautet: "Reif **wofür**?" — Shared Logic (Android + iOS): produktionsreif und von Google offiziell getragen. Compose auf iOS: stabil. Compose im Web: vielversprechend, aber noch Beta — für interne Tools ja, für die Kunden-Website mit Bedacht.

---

## Modul 2: Build-System und Gradle Multiplatform Plugin

Bevor wir Code schreiben, müssen wir verstehen, was das Build-System aus diesem Code *macht*. Alles Folgende schauen wir uns live in unserem `shared/build.gradle.kts` an.

### 2.1 Targets: Wer soll das alles bauen?

Das Kotlin-Multiplatform-Plugin arbeitet mit **Targets** — jede Deklaration im `kotlin {}`-Block erzeugt eine komplette Compile-Pipeline:

```kotlin
// shared/build.gradle.kts
kotlin {
    androidLibrary { /* AGP KMP plugin: namespace, minSdk, ... */ }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"   // import Shared (in Swift)
            isStatic = true
        }
    }

    jvm()                          // Desktop
    js { browser() }               // Kotlin/JS
    wasmJs { browser() }           // Kotlin/Wasm
}
```

Zwei Details, die gern verwirren:

* **`iosArm64` vs. `iosSimulatorArm64`:** Das echte iPhone und der Simulator auf Apple-Silicon-Macs sind *verschiedene* Targets (unterschiedliche Binaries, gleiche CPU-Architektur!). Für Intel-Macs käme noch `iosX64` dazu.
* **Android als Library-Target:** Unser Projekt nutzt das neue AGP-Plugin `com.android.kotlin.multiplatform.library` — das Shared Module ist aus Android-Sicht eine ganz normale Library mit `namespace`, `minSdk` und AAR-Output.

### 2.2 Source Sets: Die Hierarchie des Teilens

Targets legen fest, *wohin* kompiliert wird. **Source Sets** legen fest, *welcher Code* dabei mitkommt. Sie bilden eine Hierarchie:

```
                 commonMain
        ┌──────┬─────┴─────┬─────────┐
   androidMain jvmMain  appleMain   webMain (custom)
                            │        ┌───┴────┐
                         iosMain   jsMain  wasmJsMain
```

Die Regeln:

* Jeder plattformspezifische Source Set **sieht** den Code aus `commonMain` (und allen Zwischenebenen darüber) — umgekehrt nie.
* Das Plugin legt die Standard-Hierarchie (`appleMain`, `nativeMain`, …) automatisch an (*Default Hierarchy Template*).
* **Eigene Zwischenebenen** sind möglich und ein wichtiges Werkzeug: Braucht man z.B. einen Source Set für "JS **und** Wasm gemeinsam", deklariert man ihn selbst und verdrahtet ihn mit `dependsOn` — genau so entsteht der `webMain`-Source-Set in unserem `webApp` (Modul 8.2).

### 2.3 Artefakte: Was fällt hinten raus?

| Target | Artefakt | Konsumiert von |
| --- | --- | --- |
| Android | **AAR** | Gradle, wie jede Android-Library |
| iOS | **Framework / XCFramework** | Xcode (SPM, CocoaPods oder direkt) |
| JVM | **JAR** (bzw. paketierte Desktop-App) | Gradle / Installer |
| JS / Wasm | **JS-Bundle + `.wasm`** | Webpack / Browser |

Für iOS ist der Klebstoff zwischen den Welten ein Gradle-Task: Xcode ruft in einer Build Phase `embedAndSignAppleFrameworkForXcode` auf — Gradle baut das Framework, Xcode bindet es ein. Für die Distribution an andere iOS-Teams baut man ein **XCFramework** (ein Container mit Slices für Device + Simulator).

> **Faustregel:**
> iOS-Builds brauchen einen Mac — Xcode und die Apple-Toolchain gibt es nirgendwo sonst. Auf Linux/Windows **überspringt** Gradle die Apple-Targets einfach (Warnung im Log). Genau deshalb kompilieren wir den iOS-Teil im Workshop nicht live: Der Kotlin-Code ist identisch, nur der letzte Compile-Schritt fehlt uns hier im Raum. In der **CI** heißt das: Linux-Runner für Android/JVM/Web, ein macOS-Runner für die Apple-Targets — typisch als Build-Matrix (ARM64/X64) konfiguriert.

### 2.4 Dependency Management: Eine Dependency, viele Gestalten

Dependencies werden **pro Source Set** deklariert:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(libs.ktor.client.core)      // KMP library: works everywhere
    }
    androidMain.dependencies {
        implementation(libs.ktor.client.okhttp)    // Android only: OkHttp engine
    }
    iosMain.dependencies {
        implementation(libs.ktor.client.darwin)    // iOS only: Darwin engine
    }
}
```

Woher weiß Gradle, welche Variante einer KMP-Library es für welches Target laden muss? **Gradle Module Metadata**: Eine KMP-Library ist auf Maven Central kein einzelnes JAR, sondern ein Fächer von Artefakten (`-android`, `-iosarm64`, `-js`, …) plus Metadaten. `implementation(...)` in `commonMain` genügt — die Auflösung pro Target passiert automatisch.

Eine Library ist nur dann `commonMain`-tauglich, wenn sie **alle unsere Targets** unterstützt — das prüft man am schnellsten auf [klibs.io](https://klibs.io). Unterstützt sie nur einige (klassisches Beispiel: Room ohne Web), kommt sie in einen Zwischen-Source-Set (siehe 2.2) oder hinter eine `expect`/`actual`-Abstraktion (Modul 3).

---

## Modul 3: Der expect/actual-Mechanismus

### 3.1 Die Mechanik: Ein Versprechen und seine Einlösungen

`commonMain` kennt keine Plattform-APIs — aber manchmal *braucht* gemeinsamer Code plattformspezifisches Verhalten. `expect`/`actual` ist Kotlins eingebaute Antwort. Unser Projekt bringt das Schulbuchbeispiel schon mit:

```kotlin
// commonMain/Platform.kt — the contract
interface Platform {
    val name: String
}
expect fun getPlatform(): Platform
```

```kotlin
// androidMain/Platform.android.kt — one of five fulfillments
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}
actual fun getPlatform(): Platform = AndroidPlatform()
```

```kotlin
// iosMain/Platform.ios.kt
class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}
actual fun getPlatform(): Platform = IOSPlatform()
```

Die Spielregeln:

* `expect`-Deklarationen haben **keinen Body** — sie sind ein Versprechen an den Compiler.
* **Jedes Target** muss ein `actual` liefern (gleiches Package, gleiche Signatur). Fehlt eines, bricht der Build — *zur Compile-Zeit, nicht beim Kunden*. Das ist der entscheidende Unterschied zu Reflection- oder Dependency-Tricks.
* Es funktioniert für Funktionen, Properties, Klassen und Objects. Ein nützlicher Spezialfall ist `actual typealias`: Das `actual` einer erwarteten Klasse kann eine *existierende* Plattform-Klasse sein.

### 3.2 expect/actual vs. Interface + DI: Wann was?

`expect`/`actual` ist nicht die einzige Abstraktionstechnik — und nicht immer die beste:

| | `expect`/`actual` | Interface + Factory/DI |
| --- | --- | --- |
| Auflösung | Compile-Zeit | Laufzeit |
| In Tests austauschbar? | **Nein** (fest verdrahtet) | **Ja** (Fake einsetzen) |
| Boilerplate | Minimal | Interface + Implementierungen + Verdrahtung |
| Typischer Einsatz | Kleine, zustandslose Plattform-Zugriffe | Services mit Logik, alles was gemockt werden soll |

> **Faustregel:**
> `expect fun` für **Blattfunktionen** ohne Geschäftslogik: Zeitzone auslesen, Datenbankpfad ermitteln, UUID generieren. Sobald Logik dazukommt oder Tests die Implementierung austauschen sollen: **Interface in `commonMain`, Implementierungen in den Plattform-Source-Sets, Übergabe per Konstruktor.** Oft kombiniert man beides — das Interface ist common, nur die kleine Factory ist `expect`.

### 3.3 Interoperabilität: Kotlin ⇄ Swift/Objective-C

**Richtung Kotlin → Swift:** Der Kotlin/Native-Compiler generiert für das Framework **Objective-C-Header** — Swift konsumiert das Shared Module also über die ObjC-Interop-Schicht. Das funktioniert erstaunlich reibungslos, hat aber bekannte Ecken:

* `suspend`-Funktionen werden zu **Completion-Handlern** exportiert (`fun load(): Forecast` → `load { forecast, error in }`); Swift kann sie immerhin als `async` aufrufen.
* **`Flow` und Generics kommen nur typgelöscht an** — aus `Flow<WeatherState>` wird in Swift ein nacktes `Flow`. Abhilfe schaffen Wrapper-Klassen oder die Community-Library **SKIE**, die aus Flows echte Swift-`AsyncSequence`s macht (Showcase in Modul 6).
* `sealed class`-Hierarchien verlieren in Swift ihre Exhaustiveness — das `switch` braucht einen `default`-Zweig.
* **Ausblick:** JetBrains arbeitet an **Swift Export** — direkte Swift-API-Generierung ohne ObjC-Umweg. Noch experimentell, aber die Richtung ist klar.

**Richtung Kotlin → native APIs (der spannendere Teil):** In `iosMain` stehen die **kompletten Apple-SDKs als Kotlin-APIs** bereit — vorgeneriert im `platform.*`-Package:

```kotlin
// iosMain — full access to Foundation, UIKit, CoreLocation, CoreBluetooth, ...
import platform.Foundation.NSFileManager
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask

fun documentsPath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null, create = false, error = null,
    )
    return requireNotNull(url?.path)
}
```

Kein Wrapper, kein Plugin, kein Bridging-Code — Sensoren, Dateisystem, Bluetooth: alles direkt aufrufbar. Für eigene C-/ObjC-Libraries jenseits der System-SDKs gibt es das **cinterop**-Tool. In `androidMain` gilt dasselbe trivialerweise: volles Android SDK. Einzige Stolperfalle dort ist der **`Context`** — es gibt keinen globalen; er muss von der App in das Shared Module hineingereicht werden (Konstruktor-Parameter oder DI, wir sehen das in Übung 2.2).

---

## Modul 4: Multiplatform Networking mit Ktor

### 4.1 Warum Ktor?

Der erste Reflex vieler Android-Teams — Retrofit — scheidet aus: Retrofit ist **JVM-only** (Annotations + Reflection + OkHttp). **Ktor** ist JetBrains' HTTP-Stack und von Grund auf multiplattform: ein gemeinsames API in `commonMain`, austauschbare **Engines** pro Plattform, Erweiterung über ein Plugin-System.

### 4.2 Der HttpClient: Konfiguration im Common Code

```kotlin
// commonMain — the whole client is shared code
val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true   // survive API additions
            isLenient = true
        })
    }
    install(Logging) { level = LogLevel.INFO }
    install(HttpTimeout) { requestTimeoutMillis = 15_000 }
}

// Requests are suspend functions — coroutines all the way down
suspend fun fetchForecast(lat: Double, lon: Double): ForecastDto =
    client.get("https://api.open-meteo.com/v1/forecast") {
        parameter("latitude", lat)
        parameter("longitude", lon)
        parameter("current", "temperature_2m,weather_code,wind_speed_10m")
        parameter("timezone", "auto")
    }.body()
```

Bemerkenswert: **Nichts hier ist plattformspezifisch.** Asynchronität via `suspend`, Deserialisierung via `.body()`, Konfiguration via Plugins (`install`).

### 4.3 Engines: Wer macht die eigentliche Netzwerkarbeit?

Ktor selbst spricht kein TCP — das delegiert es an eine **Engine**, die pro Target als Dependency eingebunden wird:

| Target | Engine | Artefakt | Dahinter steckt |
| --- | --- | --- | --- |
| Android | `OkHttp` | `ktor-client-okhttp` | OkHttp (inkl. Interceptors!) |
| iOS | `Darwin` | `ktor-client-darwin` | `NSURLSession` (inkl. ATS, Proxy, …) |
| Desktop/JVM | `OkHttp` oder `CIO` | `ktor-client-okhttp` / `-cio` | OkHttp / Ktors eigene Coroutine-IO |
| JS / Wasm | `Js` | `ktor-client-js` | Browser `fetch` |

Liegt genau eine Engine am Classpath, findet `HttpClient {}` sie **automatisch** — deshalb steht im Common Code kein Engine-Name. Das ist gelebtes `expect`/`actual`, nur dass Gradle die Auswahl trifft statt des Compilers.

> **Faustregel:**
> Engine-Wahl ist eine **Integrations­entscheidung, keine Feature-Entscheidung**: `Darwin` respektiert App Transport Security und System-Proxies, `OkHttp` erlaubt das Weiterverwenden vorhandener Interceptor-Infrastruktur, im Browser erbt `Js` die CORS-Regeln der Seite. Das gemeinsame API bleibt identisch.

### 4.4 Typsichere De-/Serialisierung mit kotlinx.serialization

Auch hier gilt: Die JVM-Klassiker (Gson, Jackson) sind reflection-basiert und damit raus. **kotlinx.serialization** ist ein Compiler-Plugin — es generiert Serializer **zur Compile-Zeit**, funktioniert daher auf allen Targets (auch Native/Wasm, wo es keine Reflection gibt) und ist typsicher:

```kotlin
// The API's JSON:  { "current": { "time": "...", "temperature_2m": 21.4, ... } }

@Serializable
data class ForecastDto(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentDto,
)

@Serializable
data class CurrentDto(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
)
```

* `@SerialName` mappt Snake-Case-JSON auf idiomatische Kotlin-Namen.
* Fehlt ein `@Serializable`, gibt es einen **Compile-Fehler** statt einer Laufzeit-Exception — der Unterschied zwischen "der Kunde findet den Bug" und "der Compiler findet ihn".
* DTOs bleiben in der Datenschicht; ins Domain-Modell geht es per Mapping-Extension (`fun ForecastDto.toDomain(): CurrentWeather`).

**Unsere Workshop-API:** [Open-Meteo](https://open-meteo.com) — kostenlos, **ohne API-Key**, CORS-freundlich (wichtig fürs Web-Target!). Ein Request, den Sie jetzt schon im Browser testen können:

```
https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,weather_code,wind_speed_10m&timezone=auto
```

---
