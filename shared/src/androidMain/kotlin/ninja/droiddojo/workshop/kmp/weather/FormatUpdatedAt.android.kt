package ninja.droiddojo.workshop.kmp.weather

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun formatUpdatedAt(isoTime: String): String {
    val time = LocalDateTime.parse(isoTime)
    return time.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr"
}
