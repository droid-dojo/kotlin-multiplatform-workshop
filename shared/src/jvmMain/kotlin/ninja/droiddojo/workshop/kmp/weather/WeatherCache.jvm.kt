package ninja.droiddojo.workshop.kmp.weather

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun createWeatherCache(): WeatherCache =
    RoomWeatherCache(createDatabase(databaseBuilder()).weatherDao())

private fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase> {
    val appDir = File(System.getProperty("user.home"), ".kmp-workshop")
    appDir.mkdirs()
    return Room.databaseBuilder<WeatherDatabase>(
        name = File(appDir, "weather.db").absolutePath,
    ).setDriver(BundledSQLiteDriver())
}
