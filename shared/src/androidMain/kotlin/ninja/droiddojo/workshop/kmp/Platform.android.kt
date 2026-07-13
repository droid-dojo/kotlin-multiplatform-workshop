package ninja.droiddojo.workshop.kmp

import android.icu.util.TimeZone
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    // android.icu is the Android framework's built-in ICU API (API 24+)
    override val timeZoneId: String = TimeZone.getDefault().id
}

actual fun getPlatform(): Platform = AndroidPlatform()