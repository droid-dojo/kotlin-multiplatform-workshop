package ninja.droiddojo.workshop.kmp.weather

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ninja.droiddojo.workshop.kmp.getPlatform

class WeatherApi(
    private val client: HttpClient = defaultHttpClient(),
) {

    suspend fun currentWeather(latitude: Double, longitude: Double): CurrentWeather =
        client.get("$BASE_URL/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", "temperature_2m,weather_code,wind_speed_10m")
            // exercise 1.1 pays off: the platform's time zone via expect/actual
            parameter("timezone", getPlatform().timeZoneId)
        }.body<ForecastDto>().toDomain()

    private companion object {
        const val BASE_URL = "https://api.open-meteo.com"
    }
}

// No engine named here - each target brings exactly one engine on its classpath
fun defaultHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.INFO
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
    }
}
