# Workshop: Kotlin Multiplatform — Eine Codebasis, alle Plattformen

## Einführung

Willkommen zum Kotlin-Multiplatform-Workshop! In den nächsten zwei Tagen bauen wir gemeinsam **"The Shared App"** — eine Weather App, deren komplette Logik (Networking, Datenhaltung, State-Management) **einmal** in Kotlin geschrieben wird und auf **Android, iOS, Desktop und im Web** läuft.

Die Leitfrage des Workshops: **Was teilen wir — und was nicht?** KMP ist kein Alles-oder-Nichts-Framework, sondern ein Baukasten. Wer die Mechanik dahinter versteht (Targets, Source Sets, `expect`/`actual`), kann für jedes Projekt selbst entscheiden, wo Code-Sharing Gewinn bringt und wo native Arbeit die bessere Wahl ist.

### Drei Hinweise vorab

1. **Praxis in [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/):** Alle Übungen laufen auf **Android, Desktop und im Web** — dort haben wir die schnellsten Feedback-Schleifen (Desktop mit Hot Reload!). Der **iOS-/SwiftUI-Teil wird als Code-Showcase behandelt**: Wir schreiben den iOS-Code mit und besprechen ihn im Detail, kompilieren ihn aber nicht live — dafür wäre Mac-Hardware nötig. Wer ein MacBook dabei hat, kann selbstverständlich mitbauen.
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
| Interop mit Plattform-APIs | Direkt (kein Wrapper) | Über Platform Channels | Über Native Modules |

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
* Die Kern-Libraries — **[Ktor](https://ktor.io), [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines), [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization), [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)** — sind von Haus aus multiplattform.

**Google** (offizieller KMP-Support seit I/O 2024):
* Google unterstützt KMP offiziell für das **Teilen von Business-Logik zwischen Android und iOS** — und nutzt es selbst (u.a. Google Workspace).
* Die wichtigsten **[Jetpack-Libraries sind KMP-fähig](https://developer.android.com/kotlin/multiplatform)**: [Room](https://developer.android.com/kotlin/multiplatform/room), DataStore, ViewModel, Lifecycle, Paging — genau der Stack, den Android-Teams ohnehin kennen. Für uns heißt das: Wissen aus der Android-Welt (Room!) nehmen wir 1:1 mit auf die anderen Plattformen.
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
> Engine-Wahl ist eine **Integrationsentscheidung, keine Feature-Entscheidung**: `Darwin` respektiert App Transport Security und System-Proxies, `OkHttp` erlaubt das Weiterverwenden vorhandener Interceptor-Infrastruktur, im Browser erbt `Js` die CORS-Regeln der Seite. Das gemeinsame API bleibt identisch.

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

# Tag 2: Shared State, UI-Integration, Datenhaltung & Web

## Modul 5: Shared Logic in der Praxis — Coroutines, Flow & ViewModels

### 5.1 Coroutines im Common Code: Was ist überall gleich — und was nicht?

`suspend`, Structured Concurrency, `Flow` — die komplette Coroutine-Maschinerie ist Multiplatform. Unterschiede gibt es nur bei den **Dispatchern**, denn die müssen auf echte Plattform-Threads abgebildet werden:

| Dispatcher | Android | iOS | Desktop/JVM | JS / Wasm |
| --- | --- | --- | --- | --- |
| `Main` | Main Thread | Main Queue | AWT/Swing EDT¹ | Event Loop |
| `Default` | Thread-Pool | Thread-Pool | Thread-Pool | = `Main`² |
| `IO` | Thread-Pool | Thread-Pool | Thread-Pool | **existiert nicht** |

¹ via `kotlinx-coroutines-swing` — steckt bereits in unserem `desktopApp`.
² Der Browser ist single-threaded — "Nebenläufigkeit" heißt dort Kooperation auf dem Event Loop.

> **Faustregel:**
> `Dispatchers.IO` hat im Common Code nichts verloren — es existiert auf JS/Wasm nicht. Die gute Nachricht: Sie brauchen es fast nie. **Ktor und Room sind bereits main-safe** (suspend-APIs verwalten ihre Threads selbst). Für eigene CPU-Arbeit: `withContext(Dispatchers.Default)`.

### 5.2 Kotlin/Native Concurrency: Entwarnung

Wer vor 2022 mit KMP experimentiert hat, erinnert sich an `freeze()`, `InvalidMutabilityException` und die Regel "Objekte gehören einem Thread". **Dieses Memory-Model ist Geschichte.** Seit Kotlin 1.7.20 gilt auf Kotlin/Native das gleiche Modell wie auf der JVM: Objekte dürfen zwischen Threads geteilt werden, `StateFlow` & Co. funktionieren ohne Sonderregeln.

Was bleibt (und auf jeder Plattform gilt): **UI-Zustand wird am Main-Thread konsumiert.** Genau das erledigt `viewModelScope` für uns.

### 5.3 Flow & StateFlow: Der Zustands-Strom im Shared Module

Das aus Android bekannte UDF-Muster (Unidirectional Data Flow) wandert unverändert ins Shared Module:

```kotlin
// commonMain — models every state the UI can be in
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: CurrentWeather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
```

Kalte Flows aus der Datenschicht werden per `stateIn` zu heißem, teilbarem UI-State — inklusive `WhileSubscribed(5_000)` als Lifecycle-Schutz. Alles davon ist `commonMain`-Code.

### 5.4 Gemeinsame ViewModels: Jetpack ViewModel — überall

Die KMP-Variante von `androidx.lifecycle` (steckt schon in unserem Projekt!) bringt das echte Jetpack-`ViewModel` auf alle Targets:

```kotlin
// commonMain — a real Jetpack ViewModel, shared across all platforms
class WeatherViewModel(
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            _uiState.value = try {
                WeatherUiState.Success(repository.currentWeather())
            } catch (e: Exception) {
                WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

* `viewModelScope` wird pro Plattform korrekt an den Main-Dispatcher gebunden und beim Wegwerfen des ViewModels gecancelt.
* In Compose (egal ob Android, Desktop, Web oder iOS) kommt das ViewModel per `viewModel { ... }` in die Composition — **eine State-Verwaltung für alle Plattformen**.
* Konsumiert **SwiftUI** das ViewModel direkt, gilt die Interop-Einschränkung aus Modul 3 (typgelöschte Flows) — wie man damit umgeht, zeigt der Showcase in 6.3.

---

## Modul 6: UI-Integration — Compose Multiplatform & SwiftUI

### 6.1 Compose Multiplatform: Eine UI, vier Einstiegspunkte

Unsere `App()`-Composable lebt in `commonMain` — jede Plattform braucht nur noch einen Adapter an ihr Fenstersystem:

```kotlin
// androidApp — MainActivity.kt
setContent { App() }

// shared/iosMain — MainViewController.kt (consumed by SwiftUI, see 6.3)
fun MainViewController() = ComposeUIViewController { App() }

// desktopApp — main.kt
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "KMPWorkshop") { App() }
}

// webApp — main.kt
ComposeViewport { App() }
```

Das ist die gesamte plattformspezifische UI-Arbeit. Material 3, `remember`, `LaunchedEffect`, `collectAsStateWithLifecycle` — alles läuft identisch auf allen Targets. Auf dem Desktop kommt mit **Compose Hot Reload** die schnellste Iterationsschleife dazu: UI-Änderung speichern, App aktualisiert sich live — unser Standard-Workflow in den Übungen.

### 6.2 State bis in die UI: Das Gesamtbild

```
   Ktor ──▶ Repository ──▶ WeatherViewModel ──▶ StateFlow<WeatherUiState>
  (commonMain)  (commonMain)   (commonMain)              │
                                                         ▼
                              ┌──────────────────────────────────┐
                              │  Compose (common):               │
                              │  val state by                    │
                              │    vm.uiState.collectAsState...()│
                              │  when (state) { ... }            │
                              └──────────────────────────────────┘
```

Der `when`-Block über dem `sealed interface` ist dabei mehr als Syntax: Der Compiler **erzwingt**, dass die UI jeden Zustand behandelt — Loading-Spinner und Error-Screen können nicht mehr "vergessen" werden.

### 6.3 SwiftUI-Showcase: Kotlin-State in Swift konsumieren

> **Hinweis:** Diesen Teil behandeln wir als **Code-Showcase** — wir lesen und diskutieren den Code gemeinsam, bauen ihn aber nicht live (kein Mac im Kursraum). Der Code liegt vollständig im Repository unter `iosApp/`.

**Variante A — Compose-UI in SwiftUI hosten** (so ist unser Projekt verdrahtet): Die geteilte Compose-UI wird als `UIViewController` verpackt; SwiftUI ist nur noch die äußerste Schale:

```swift
// iosApp — ContentView.swift
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()   // Kotlin function!
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

**Variante B — native SwiftUI über dem Shared ViewModel** (der klassische "Share logic, not UI"-Schnitt): Ein `ObservableObject` sammelt den `StateFlow` ein und übersetzt ihn in `@Published`-Properties:

```swift
// iosApp — plain SwiftUI consuming the shared StateFlow
@MainActor
final class WeatherObservable: ObservableObject {
    @Published var state: WeatherUiState = WeatherUiStateLoading()
    private let viewModel = WeatherViewModel(repository: WeatherRepository())

    func activate() async {
        // With SKIE, the StateFlow becomes a native AsyncSequence:
        for await state in viewModel.uiState {
            self.state = state
        }
    }
}

struct WeatherView: View {
    @StateObject var observable = WeatherObservable()

    var body: some View {
        Group {
            switch observable.state {
            case is WeatherUiStateLoading: ProgressView()
            case let success as WeatherUiStateSuccess: Text("\(success.weather.temperature) °C")
            case let error as WeatherUiStateError: Text(error.message)
            default: EmptyView()   // Swift can't prove exhaustiveness (module 3!)
            }
        }
        .task { await observable.activate() }
    }
}
```

Genau hier zahlt sich die Theorie aus Modul 3 aus: Das `default:` ist kein Stilfehler, sondern die typgelöschte ObjC-Interop — und die `for await`-Zeile funktioniert so elegant nur mit **SKIE** (ohne: Callback-basierte `watch`-Wrapper von Hand).

### 6.4 Mix & Match und Ausblick

* **Native Views in Compose:** `UIKitView` (iOS) bzw. `HtmlElementView` (Web) betten native Komponenten — Karten, Video, WebViews — in die gemeinsame UI ein.
* **Schrittweise Migration** ist damit in beide Richtungen möglich: erst ein Screen in Compose, der Rest nativ — oder umgekehrt.
* **Compose Multiplatform heute:** Android & Desktop stabil, iOS stabil, Web Beta. Ein Team, das Jetpack Compose kann, kann Compose Multiplatform — die Lernkurve ist im Wesentlichen null.

---

## Modul 7: Datenhaltung mit Room

### 7.1 Warum Room? (Eine Anpassung der Agenda)

In der ursprünglichen Agenda stand an dieser Stelle [SQLDelight](https://sqldelight.github.io/sqldelight/) — wir arbeiten stattdessen mit **Room 3.0**, und das aus drei Gründen:

1. **Ihr Android-Wissen zählt doppelt:** Room ist der Jetpack-Standard, den Android-Teams ohnehin kennen — `@Entity`, `@Dao`, `@Query` funktionieren in KMP exakt wie gewohnt.
2. **First-Party-Support:** Room ist seit 2.7 offiziell multiplattform und Teil von Googles KMP-Strategie (Modul 1.4).
3. **Room 3.0 ist das KMP-Release** (stabil seit Juli 2026, neues Package `androidx.room3`): Kotlin-only Codegen, **Coroutines-first** (blockierende DAO-Funktionen sind abgeschafft — `suspend` oder `Flow`, sonst Compile-Fehler) und erstmals **Web-Support (JS/Wasm)**. Damit deckt *eine* Datenbank-Library alle fünf Targets unseres Projekts ab.

| | Room 3 | SQLDelight |
| --- | --- | --- |
| Ansatz | **Entity-first** (Annotations → SQL) | **SQL-first** (`.sq`-Dateien → Kotlin) |
| Herausgeber | Google (Jetpack) | CashApp (Square) |
| Web-Support | ja (Web Worker + OPFS) | ja (Web Worker Driver) |
| Für Android-Teams | kein Umlernen | neues Toolset |

SQLDelight bleibt eine ausgezeichnete Library — wer echtes SQL bevorzugt oder exotischere Targets braucht, greift dort zu. Die Architektur-Muster dieses Moduls (Single Source of Truth, Flow aus der DB) sind identisch.

### 7.2 Setup: KSP, Plugin, Treiber

Room braucht drei Zutaten (vollständige Einträge in **Anhang A**):

1. **KSP** — der Annotation-Prozessor, konfiguriert **pro Target** (`kspAndroid`, `kspIosArm64`, `kspJs`, …).
2. Das **Room-Gradle-Plugin** (`androidx.room3`) mit `schemaDirectory` für Migrations-Schemata.
3. Einen **SQLite-Treiber** — und der ist Plattform-Sache:
   * Android, iOS, Desktop: `BundledSQLiteDriver` (`androidx.sqlite:sqlite-bundled`) — kompiliert SQLite direkt mit ein. Damit läuft überall dieselbe SQLite-Version statt "was auch immer das OS mitbringt" (Android 7 liefert SQLite 3.9 von 2015!).
   * Browser: `WebWorkerSQLiteDriver` (`androidx.sqlite:sqlite-web`) — SQLite läuft in einem **Web Worker** und persistiert ins **Origin Private File System (OPFS)**, den Browser-Speicher für genau solche Fälle.

### 7.3 Entity, DAO, Database: Fast wie zu Hause

```kotlin
// commonMain — identical to Android Room, plus Flow support
@Entity
data class WeatherEntity(
    @PrimaryKey val locationKey: String,
    val temperature: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val updatedAt: String,
)

@Dao
interface WeatherDao {
    @Upsert
    suspend fun upsert(weather: WeatherEntity)

    @Query("SELECT * FROM WeatherEntity WHERE locationKey = :key")
    fun observe(key: String): Flow<WeatherEntity?>
}
```

Der einzige sichtbare Unterschied zu Android-Room 2.x: Alle Imports kommen aus **`androidx.room3.*`** — und der `@Dao` darf **keine blockierenden Funktionen** mehr enthalten (`suspend` oder `Flow`, sonst Compile-Fehler). Unser Code oben erfüllt das bereits — wer nach Modul 5 gearbeitet hat, schreibt automatisch Room-3-konform.

Zwei KMP-Besonderheiten bei der Database-Klasse:

```kotlin
// commonMain
@Database(entities = [WeatherEntity::class], version = 1)
@ConstructedBy(WeatherDatabaseConstructor::class)          // KMP-specific!
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}

// The Room compiler generates the actuals — one per target.
@Suppress("KotlinNoActualForExpect")
expect object WeatherDatabaseConstructor : RoomDatabaseConstructor<WeatherDatabase> {
    override fun initialize(): WeatherDatabase
}
```

`@ConstructedBy` ersetzt die Reflection, mit der Room auf Android die generierte Implementierung findet — auf Kotlin/Native gibt es keine Reflection (Modul 1.2!), also übernimmt der Compiler via `expect object`.

**Datenbank-Pfad und Treiber sind Plattform-Sache** — ein Paradebeispiel für die Faustregel aus Modul 3.2 (kleine, zustandslose Zugriffe → `expect`/`actual`):

```kotlin
// commonMain
expect fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase>

// androidMain — needs the Context (injected, see exercise 2.2)
actual fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase> =
    Room.databaseBuilder<WeatherDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("weather.db").absolutePath,
    ).setDriver(BundledSQLiteDriver())

// iosMain — Documents directory via NSFileManager (module 3.3!)
actual fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase> =
    Room.databaseBuilder<WeatherDatabase>(
        name = documentsPath() + "/weather.db",
    ).setDriver(BundledSQLiteDriver())

// jsMain / wasmJsMain — SQLite in a Web Worker, persisted to OPFS
actual fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase> =
    Room.databaseBuilder<WeatherDatabase>(name = "weather.db")
        .setDriver(WebWorkerSQLiteDriver(worker))

// Shared assembly, common again:
fun createDatabase(): WeatherDatabase =
    databaseBuilder()
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
```

Fällt Ihnen etwas auf? Die `expect`-Signatur ist **parameterlos** — aber jedes `actual` braucht andere Zutaten: Android einen `Context`, iOS einen Pfad aus `NSFileManager`, das Web einen Worker. Zusätzliche Parameter darf ein `actual` nicht einführen, die Signatur ist Teil des Kontrakts (Modul 3.1). Jede Plattform muss sich ihre Abhängigkeiten also **selbst beschaffen**: iOS und Web fragen direkt ihre System-APIs, aber auf Android gibt es keinen globalen `Context` — er wird beim App-Start **einmal von außen hineingereicht** (etwa in eine Top-Level-Referenz `appContext` in `androidMain`, gesetzt aus `Application.onCreate()`; genau so bauen wir es in Übung 2.2). Dieses Muster — gemeinsame Signatur, plattform-eigene Beschaffung der Abhängigkeiten — ist der Standardweg, wann immer ein `actual` mehr braucht, als die `expect`-Deklaration hergibt.

> **Faustregel:**
> Room 3.0 ist zum Workshop-Zeitpunkt wenige Wochen alt. Android, iOS und Desktop sind der seit Room 2.7 erprobte Pfad — das Web-Target ist Neuland (Worker-Setup, asynchrone Treiber-APIs) und in der Übung bewusst als **Bonus** markiert. Genau so würde man es auch im Projekt einführen: erprobte Targets zuerst, das neueste Target hinter einem Feature-Branch.

### 7.4 Offline-First: Die Datenbank als Single Source of Truth

Mit DB und API entsteht das Single-Source-of-Truth-Muster — die UI liest **ausschließlich** aus der Datenbank, das Netzwerk *aktualisiert* nur:

```kotlin
// commonMain
class WeatherRepository(
    private val api: WeatherApi,
    private val dao: WeatherDao,
) {
    // UI observes the database — works offline by definition
    fun observeWeather(key: String): Flow<CurrentWeather?> =
        dao.observe(key).map { it?.toDomain() }

    // Network only refreshes the single source of truth
    suspend fun refresh(key: String, lat: Double, lon: Double) {
        val dto = api.fetchForecast(lat, lon)
        dao.upsert(dto.toEntity(key))
    }
}
```

Flugzeugmodus? Die App zeigt den letzten Stand statt eines Fehler-Screens — auf **jeder** Plattform, denn das Muster lebt komplett in `commonMain`.

### 7.5 Migrationen: Einmal definiert, überall versioniert

Auch das Schema-Management ist geteilt — die Datenbank-Version ist auf allen Plattformen dieselbe:

* Das Gradle-Plugin exportiert das Schema als JSON nach `schemas/` (→ gehört ins Git! Es ist die Historie, gegen die migriert wird).
* **Auto-Migrations** (`@Database(autoMigrations = [AutoMigration(1, 2)])`) decken einfache Fälle (Spalte hinzufügen) ab.
* Für alles andere: klassische `Migration`-Objekte mit SQL — einmal geschrieben, auf jedem Target ausgeführt.

> **Faustregel:**
> Ohne registrierte Migration wirft Room beim Schema-Sprung — auf dem iPhone genauso wie auf Android. Der Multiplatform-Bonus: Sie schreiben und testen die Migration **einmal** (JVM-Test!), statt sie in zwei Codebasen synchron halten zu müssen.

---

## Modul 8: Web-Targets — Kotlin/JS und Kotlin/Wasm

### 8.1 Zwei Wege in den Browser

Unser Projekt kompiliert das Web-Target doppelt — und genau so ist es gedacht:

| | Kotlin/**JS** | Kotlin/**Wasm** |
| --- | --- | --- |
| Output | JavaScript | WebAssembly (mit GC) |
| Performance | gut | nahe JVM — oft ~2× schneller als JS |
| Browser-Support | universell | moderne Browser (WasmGC) |
| Status | stabil | Beta, Standard-Empfehlung für Neues |

**WebAssembly-Hintergrund:** Kotlin/Wasm setzt auf **WasmGC** — WebAssembly mit eingebautem Garbage Collector, sodass Kotlin keinen eigenen GC mitliefern muss. Seit Ende 2024 unterstützen **alle großen Browser** WasmGC (Chrome/Edge, Firefox, Safari ab 18.2). Für die Restmenge alter Browser bleibt das JS-Target als Fallback — deshalb die Doppelstrategie.

### 8.2 Der webMain-Trick in unserem Projekt

JS und Wasm teilen sich fast allen Code — unser `webApp` nutzt dafür einen gemeinsamen `webMain`-Source-Set (eine *custom* Zwischenebene, Modul 2.2 in Aktion):

```
        commonMain
            │
         webMain      ← webApp code lives here once
        ┌───┴────┐
     jsMain   wasmJsMain
```

Die Compose-UI landet per `ComposeViewport { App() }` auf einem Canvas — gerendert von Skia (via Wasm), nicht als DOM-Bäume. Konsequenz: pixelidentisches Rendering zur Desktop-/Mobile-App, aber die Seite verhält sich wie eine App, nicht wie ein Dokument (SEO, Textselektion!). Compose Web ist für **App-artige** Anwendungen gedacht, nicht für Content-Seiten.

### 8.3 DOM-Interaktion: Web-APIs direkt aus Kotlin

Auch ohne Compose sind sämtliche Browser-APIs aus Kotlin erreichbar — typsicher über die offiziellen **kotlin-wrappers** (`kotlin-browser`, bereits im Projekt):

```kotlin
// jsMain / wasmJsMain — the browser API, typed
import web.dom.document

fun updateTitle(city: String) {
    document.title = "Weather — $city"
}
```

Für alles jenseits der Wrapper: `external`-Deklarationen typisieren beliebige JS-APIs, `@JsExport` macht umgekehrt Kotlin-Funktionen für bestehendes JavaScript aufrufbar — der Migrationsweg, um KMP-Logik in eine existierende Web-App zu heben (Modul 9).

> **Faustregel:**
> Interop-Grenze im Blick behalten: Auf Wasm sind JS-Aufrufe teurer als auf dem JS-Target (Boundary-Crossing). Viele kleine DOM-Zugriffe in heißen Schleifen → JS-Target oder API-Design überdenken. Rechenlastige Shared Logic → genau dafür ist Wasm da.

---

## Modul 9: Final Roadmap — Der Migrationspfad zu KMP

Zum Abschluss die Frage, die Sie in Ihre Projekte mitnehmen: **Wie kommt eine bestehende Single-Platform-App zu KMP?** Die Antwort ist nie "Rewrite", sondern immer inkrementell — das Spektrum aus Modul 1.1 wird zur Roadmap:

### 9.1 Die vier Etappen

1. **Ein Modul, ein Feature (Pilot):** Ein abgegrenztes Stück Logik — ein Validierungs-Modul, ein API-Client — wird als Shared Module extrahiert. Die bestehende App konsumiert es als ganz normales Artefakt: **AAR** für Android, **XCFramework** via SPM für iOS, **npm-Package** fürs Web. Die iOS-Kollegen merken idealerweise nur: eine neue Dependency.
2. **Die Datenschicht:** Models → Networking (Retrofit ⇒ Ktor, Gson ⇒ kotlinx.serialization) → Persistenz (Room ist es oft schon!). Nach dieser Etappe ist der teuerste Code geteilt.
3. **State & ViewModels:** Mit den KMP-Jetpack-Libraries wandern ViewModels und UDF-State ins Shared Module. Ab hier sind die nativen Apps nur noch UI.
4. **UI (optional):** Compose Multiplatform — Screen für Screen, beginnend mit den am wenigsten plattform-idiomatischen (Settings, Formulare, interne Tools).

### 9.2 Die ehrlichen Erfolgsfaktoren

* **Das iOS-Team gehört ins Boot — ab Etappe 1.** KMP scheitert selten an der Technik, öfter daran, dass es sich für iOS-Entwickler wie eine feindliche Übernahme anfühlt. Gegenmittel: saubere API-Grenzen (SKIE!), gemeinsame Code-Ownership, und Swift-Kenntnisse im Kotlin-Team ernst nehmen.
* **Build-Infrastruktur zuerst:** macOS-CI-Runner, Artefakt-Publishing und Versionierung des Shared Modules müssen stehen, *bevor* das zweite Feature geteilt wird.
* **Exit-Strategie behalten:** Solange nur Logik geteilt wird, ist das Shared Module ersetzbar (Modul 1.1) — dieses Argument überzeugt auch skeptische Architektur-Boards.

### 9.3 Checkliste für Ihr Projekt

- [ ] Kandidaten identifiziert? (Viel Logik, wenig UI, auf beiden Plattformen doppelt gepflegt)
- [ ] Libraries KMP-fähig? ([klibs.io](https://klibs.io) — Retrofit/Gson/RxJava haben direkte KMP-Pendants)
- [ ] CI kann macOS-Builds? (ARM64-Runner, `konan`-Caching)
- [ ] Team-Setup geklärt? (Wer reviewt `iosMain`-Code?)
- [ ] Pilot klein genug, um in einem Sprint zu scheitern — und klein genug, um in einem Sprint zu überzeugen?

---

## Anhang A: Setup & Dependencies für die Übungen

### Schritt 1: `gradle/libs.versions.toml` erweitern

```toml
[versions]
# ... existing versions ...

# Übung 1.2: Networking
ktor = "3.5.0"
kotlinx-serialization = "1.11.0"

# Übung 2.2: Room
ksp = "2.3.9"
room = "3.0.0"
sqlite = "2.7.0"

[libraries]
# ... existing libraries ...

# Übung 1.2: Ktor + kotlinx.serialization
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-contentNegotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinxJson = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }     # Android + Desktop
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }     # iOS
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }             # JS + Wasm
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Übung 2.2: Room 3 + SQLite-Treiber
androidx-room-runtime = { module = "androidx.room3:room3-runtime", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room" }
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }
androidx-sqlite-web = { module = "androidx.sqlite:sqlite-web", version.ref = "sqlite" }

[plugins]
# ... existing plugins ...
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
androidx-room = { id = "androidx.room3", version.ref = "room" }
```

### Schritt 2 (Übung 1.2): `shared/build.gradle.kts` erweitern

```kotlin
plugins {
    // ... existing plugins ...
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ... existing dependencies ...
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
```

### Schritt 3 (Übung 2.2): Room 3 einbinden

Room 3 unterstützt alle unsere Targets — die Runtime kommt daher nach `commonMain`, nur die **Treiber** sind Plattform-Sache (Modul 7.2). Der KSP-Compiler wird pro Target registriert:

```kotlin
plugins {
    // ... existing plugins ...
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ... existing dependencies ...
            implementation(libs.androidx.room.runtime)
        }
        androidMain.dependencies { implementation(libs.androidx.sqlite.bundled) }
        iosMain.dependencies { implementation(libs.androidx.sqlite.bundled) }
        jvmMain.dependencies { implementation(libs.androidx.sqlite.bundled) }
        jsMain.dependencies { implementation(libs.androidx.sqlite.web) }      // nur für den Bonus (Web-Treiber)
        wasmJsMain.dependencies { implementation(libs.androidx.sqlite.web) }  // nur für den Bonus (Web-Treiber)
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    // Auch ohne Web-Treiber Pflicht: Room-Code in commonMain kompiliert für ALLE Targets,
    // der Compiler muss die Database-Constructor-actuals also auch für JS/Wasm generieren.
    add("kspJs", libs.androidx.room.compiler)
    add("kspWasmJs", libs.androidx.room.compiler)
}
```

---

## Anhang B: Spickzettel — Kotlin kompakt

Für alle, deren Kotlin etwas eingestaubt ist — die Idiome, die in unseren Übungen ständig vorkommen:

```kotlin
// val = read-only, var = mutable — default to val
val city = "Berlin"

// Data class: equals/hashCode/copy for free — our models & DTOs
data class CurrentWeather(val temperature: Double, val windSpeed: Double)

// Null safety: ?. safe call, ?: default, let for "if not null"
val temp = weather?.temperature ?: 0.0

// when as expression — exhaustive over sealed types (compiler-checked!)
val label = when (state) {
    is WeatherUiState.Loading -> "…"
    is WeatherUiState.Success -> "${state.weather.temperature} °C"
    is WeatherUiState.Error -> state.message
}

// Trailing lambda — the syntax behind Compose & coroutine builders
scope.launch { refresh() }
Button(onClick = { vm.refresh() }) { Text("Reload") }

// Extension function — mapping DTO → domain model
fun ForecastDto.toDomain(): CurrentWeather =
    CurrentWeather(current.temperature, current.windSpeed)

// suspend: may pause without blocking — only callable from coroutines
suspend fun fetchForecast(lat: Double, lon: Double): ForecastDto

// object = singleton, companion object = "statics"
object AppDependencies { val repository = ... }
```

> **Faustregel:**
> Wenn im Workshop eine Zeile Kotlin unklar ist: sofort fragen — die Syntax soll nie im Weg zum eigentlichen Thema (Multiplatform!) stehen.
