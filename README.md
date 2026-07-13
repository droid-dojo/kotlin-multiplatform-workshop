# 🏁 Lab 2 — Final: The Shared App ist fertig!

**Dieser Branch enthält die Musterlösung von Übung 2.3** — und damit den Endstand des Workshops. Eine Wetter-App mit geteiltem Networking, geteilter Datenbank, geteiltem State-Management und geteilter UI:

| Schicht | Technologie | Wo sie lebt |
| --- | --- | --- |
| UI | Compose Multiplatform | `commonMain` (`WeatherScreen`) |
| State | Jetpack ViewModel + StateFlow | `commonMain` (`WeatherViewModel`) |
| Daten | [Room 3](https://developer.android.com/kotlin/multiplatform/room) als Single Source of Truth | `commonMain` + Treiber pro Target |
| Netzwerk | [Ktor](https://ktor.io) + [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | `commonMain` + Engine pro Target |
| Plattform-Zugriffe | `expect`/`actual` | `timeZoneId`, `formatUpdatedAt`, `createWeatherCache` |

Die Plattform-Einstiegspunkte (`androidApp`, `iosApp`, `desktopApp`, `webApp`) sind zusammen keine 100 Zeilen — der Rest ist geteilt.

## 🔍 Sehenswert in der Lösung von Übung 2.3

* `weather/FormatUpdatedAt.kt` + fünf `actual`s: JVM-Zeit-API, `NSDateFormatter`, Browser-`Date` — einmal quer durch alle nativen SDKs.
* `git log` dieses Branches: Der „nur auf dem Desktop getestet"-Commit ist absichtlich Teil der Historie. So sieht der Fehler im echten Leben aus — und so schnell ist er mit `expect`/`actual` behoben.

## 🗺 Wie geht es in Ihren Projekten weiter?

Die **Final Roadmap** steht im [HANDOUT.md](HANDOUT.md), **Modul 9**: der etappenweise Migrationspfad von einer Single-Platform-App zu KMP — inklusive Checkliste für den Pilot-Kandidaten in Ihrem Team.

Wer noch Energie hat:

* **Bonus aus Übung 2.2:** Den `InMemoryWeatherCache` im Web durch den `WebWorkerSQLiteDriver` ersetzen — dann persistiert auch der Browser.
* **Eigene Stadt:** Koordinaten ins Repository, oder gleich eine Stadt-Auswahl in die UI.
* **SwiftUI-Showcase:** Der Code aus Modul 6.3 wartet in `iosApp/` auf alle, die einen Mac zur Hand haben.

---

**Danke fürs Mitbauen — und viel Erfolg mit Kotlin Multiplatform!** 🚀
