package ninja.droiddojo.workshop.kmp.weather

data class CurrentWeather(
    val temperature: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val time: String,
)

fun ForecastDto.toDomain(): CurrentWeather = CurrentWeather(
    temperature = current.temperature,
    windSpeed = current.windSpeed,
    weatherCode = current.weatherCode,
    time = current.time,
)
