package ninja.droiddojo.workshop.kmp

import java.util.TimeZone

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    // kotlinx-datetime's TimeZone.currentSystemDefault().id would work here too
    override val timeZoneId: String = TimeZone.getDefault().id
}

actual fun getPlatform(): Platform = JVMPlatform()