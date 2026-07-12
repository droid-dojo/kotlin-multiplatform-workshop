package ninja.droiddojo.workshop.kmp.weather

class WeatherRepository(
    private val api: WeatherApi = WeatherApi(),
) {

    suspend fun currentWeather(): CurrentWeather =
        api.currentWeather(BERLIN_LATITUDE, BERLIN_LONGITUDE)

    private companion object {
        const val BERLIN_LATITUDE = 52.52
        const val BERLIN_LONGITUDE = 13.41
    }
}
