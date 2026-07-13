package ninja.droiddojo.workshop.kmp.weather

import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val api: WeatherApi = WeatherApi(),
    private val cache: WeatherCache = weatherCache,
) {

    // The UI observes the database — offline capability by design (single source of truth)
    fun observeWeather(): Flow<CurrentWeather?> = cache.observe()

    // The network only refreshes the single source of truth
    suspend fun refresh() {
        cache.store(api.currentWeather(BERLIN_LATITUDE, BERLIN_LONGITUDE))
    }

    private companion object {
        const val BERLIN_LATITUDE = 52.52
        const val BERLIN_LONGITUDE = 13.41
    }
}
