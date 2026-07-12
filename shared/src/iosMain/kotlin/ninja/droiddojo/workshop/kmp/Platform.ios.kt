package ninja.droiddojo.workshop.kmp

import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val timeZoneId: String = NSTimeZone.localTimeZone.name
}

actual fun getPlatform(): Platform = IOSPlatform()