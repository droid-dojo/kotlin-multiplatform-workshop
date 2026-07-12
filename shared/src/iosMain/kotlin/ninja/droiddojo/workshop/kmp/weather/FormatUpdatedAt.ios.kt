package ninja.droiddojo.workshop.kmp.weather

import platform.Foundation.NSDateFormatter

actual fun formatUpdatedAt(isoTime: String): String {
    val parser = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm" }
    val date = parser.dateFromString(isoTime) ?: return isoTime
    val formatter = NSDateFormatter().apply { dateFormat = "HH:mm" }
    return formatter.stringFromDate(date) + " Uhr"
}
