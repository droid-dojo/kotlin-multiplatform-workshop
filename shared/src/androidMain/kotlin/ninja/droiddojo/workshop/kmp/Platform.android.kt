package ninja.droiddojo.workshop.kmp

import android.os.Build
import java.util.TimeZone

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val timeZoneId: String = TimeZone.getDefault().id
}

actual fun getPlatform(): Platform = AndroidPlatform()