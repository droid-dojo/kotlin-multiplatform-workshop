package ninja.droiddojo.workshop.kmp.weather

import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val api: WeatherApi = WeatherApi(),
    private val cache: WeatherCache = weatherCache,
) {

    // The UI observes the database - offline capability by design (single source of truth)
    fun observeWeather(): Flow<CurrentWeather?> = cache.observe()

    // The network only refreshes the single source of truth
    suspend fun refresh() {
        cache.store(api.currentWeather(LATITUDE, LONGITUDE))
    }

    private companion object {
        // Your town goes here - these coordinates mark the geographic centre of Germany
        const val LATITUDE = 51.16
        const val LONGITUDE = 10.45
    }
}
