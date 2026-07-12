package ninja.droiddojo.workshop.kmp.weather

// Open-Meteo delivers local time without a zone, e.g. "2026-07-13T10:15".
// Formatting is platform business — in production, kotlinx-datetime would
// replace all five actuals with one common implementation.
expect fun formatUpdatedAt(isoTime: String): String
