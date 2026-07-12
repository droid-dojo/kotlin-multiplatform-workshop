package ninja.droiddojo.workshop.kmp.weather

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// The repository only ever sees this interface — Room is an implementation detail
interface WeatherCache {
    fun observe(): Flow<CurrentWeather?>
    suspend fun store(weather: CurrentWeather)
}

expect fun createWeatherCache(): WeatherCache

// One cache per process — every repository shares the same database connection
val weatherCache: WeatherCache by lazy { createWeatherCache() }

class RoomWeatherCache(private val dao: WeatherDao) : WeatherCache {

    override fun observe(): Flow<CurrentWeather?> =
        dao.observe(LOCATION_KEY).map { it?.toDomain() }

    override suspend fun store(weather: CurrentWeather) {
        dao.upsert(weather.toEntity(LOCATION_KEY))
    }

    private companion object {
        const val LOCATION_KEY = "berlin"
    }
}

class InMemoryWeatherCache : WeatherCache {

    private val state = MutableStateFlow<CurrentWeather?>(null)

    override fun observe(): Flow<CurrentWeather?> = state

    override suspend fun store(weather: CurrentWeather) {
        state.value = weather
    }
}
