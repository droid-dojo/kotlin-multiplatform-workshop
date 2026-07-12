package ninja.droiddojo.workshop.kmp

interface Platform {
    val name: String
    val timeZoneId: String
}

expect fun getPlatform(): Platform