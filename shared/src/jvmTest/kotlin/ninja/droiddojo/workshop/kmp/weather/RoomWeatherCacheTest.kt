package ninja.droiddojo.workshop.kmp.weather

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomWeatherCacheTest {

    @Test
    fun storedWeatherIsObservable() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<WeatherDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        val cache = RoomWeatherCache(database.weatherDao())

        assertNull(cache.observe().first())

        val weather = CurrentWeather(
            temperature = 21.4,
            windSpeed = 12.7,
            weatherCode = 3,
            time = "2026-07-13T10:15",
        )
        cache.store(weather)

        assertEquals(weather, cache.observe().first())
        database.close()
    }
}
