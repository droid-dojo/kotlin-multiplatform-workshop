package ninja.droiddojo.workshop.kmp.weather

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// There is no global Context in a KMP module - the app hands it in once at startup
private lateinit var appContext: Context

fun initWeatherDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun createWeatherCache(): WeatherCache =
    RoomWeatherCache(createDatabase(databaseBuilder()).weatherDao())

private fun databaseBuilder(): RoomDatabase.Builder<WeatherDatabase> =
    Room.databaseBuilder<WeatherDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("weather.db").absolutePath,
    ).setDriver(BundledSQLiteDriver())
