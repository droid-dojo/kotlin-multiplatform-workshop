# ✅ Lab 1 — Final: Tag 1 geschafft!

**Dieser Branch enthält die Musterlösung von Übung 1.2** — und damit den kompletten Stand von Tag 1:

* `expect`/`actual` über fünf Targets (`Platform.timeZoneId`) — Übung 1.1
* Geteilte Netzwerk-Schicht mit [Ktor](https://ktor.io) + [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) gegen [Open-Meteo](https://open-meteo.com) — Übung 1.2

Sehenswert beim Vergleichen (`git diff lab-1-uebung-1.2 -- shared/`):

* `shared/src/commonMain/.../weather/` — DTOs, Domain-Modell und `WeatherApi`, komplett plattformneutral.
* `shared/build.gradle.kts` — pro Source Set genau eine Ktor-Engine; `commonMain` kennt keine.
* `androidApp/src/main/AndroidManifest.xml` — die `INTERNET`-Permission, die gern vergessen wird.
* `shared/src/commonTest/.../WeatherMappingTest.kt` — ein Test, der auf jedem Target laufen könnte.

---

**Weiter geht's:** Tag 2 startet im Branch `lab-2-uebung-2.1` — dort bauen wir aus dem Quick-&-Dirty-`LaunchedEffect` ein echtes **Shared ViewModel mit StateFlow** und eine richtige Compose-UI.
