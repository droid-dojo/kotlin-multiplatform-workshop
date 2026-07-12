package ninja.droiddojo.workshop.kmp.weather

actual fun formatUpdatedAt(isoTime: String): String = formatViaDate(isoTime) + " Uhr"

private fun formatViaDate(iso: String): String =
    js("new Date(iso).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})")
