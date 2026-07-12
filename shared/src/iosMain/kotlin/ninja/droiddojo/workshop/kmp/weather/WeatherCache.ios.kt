package ninja.droiddojo.workshop.kmp.weather

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun createWeatherCache(): WeatherCache =
    RoomWeatherCache(createDatabase(databaseBuilder()).weatherDao())

private fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase> =
    Room.databaseBuilder<WeatherDatabase>(
        name = documentsPath() + "/weather.db",
    ).setDriver(BundledSQLiteDriver())

private fun documentsPath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(url?.path)
}
