package ninja.droiddojo.workshop.kmp

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val timeZoneId: String = intlTimeZone()
}

// In Kotlin/Wasm, js(...) must be the sole expression of a function body
private fun intlTimeZone(): String =
    js("Intl.DateTimeFormat().resolvedOptions().timeZone")

actual fun getPlatform(): Platform = WasmPlatform()