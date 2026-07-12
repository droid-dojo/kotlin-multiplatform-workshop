package ninja.droiddojo.workshop.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform