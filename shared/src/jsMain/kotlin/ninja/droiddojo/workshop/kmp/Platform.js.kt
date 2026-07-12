package ninja.droiddojo.workshop.kmp

import web.navigator.navigator

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"

    override val timeZoneId: String = intlTimeZone()
}

private fun intlTimeZone(): String =
    js("Intl.DateTimeFormat().resolvedOptions().timeZone").unsafeCast<String>()

actual fun getPlatform(): Platform = JsPlatform()