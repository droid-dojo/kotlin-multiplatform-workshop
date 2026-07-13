package ninja.droiddojo.workshop.kmp.weather

class WeatherRepository(
    private val api: WeatherApi = WeatherApi(),
) {

    suspend fun currentWeather(): CurrentWeather =
        api.currentWeather(LATITUDE, LONGITUDE)

    private companion object {
        // Your town goes here - these coordinates mark the geographic centre of Germany
        const val LATITUDE = 51.16
        const val LONGITUDE = 10.45
    }
}
