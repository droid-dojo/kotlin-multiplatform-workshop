package ninja.droiddojo.workshop.kmp.weather

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherMappingTest {

    // Real Open-Meteo shape, including fields our DTOs don't model
    private val sampleJson = """
        {
          "latitude": 52.52,
          "longitude": 13.41,
          "generationtime_ms": 0.119,
          "utc_offset_seconds": 7200,
          "current_units": { "temperature_2m": "°C" },
          "current": {
            "time": "2026-07-13T10:15",
            "temperature_2m": 21.4,
            "weather_code": 3,
            "wind_speed_10m": 12.7
          }
        }
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun forecastJsonMapsToDomainModel() {
        val weather = json.decodeFromString<ForecastDto>(sampleJson).toDomain()

        assertEquals(21.4, weather.temperature)
        assertEquals(12.7, weather.windSpeed)
        assertEquals(3, weather.weatherCode)
        assertEquals("2026-07-13T10:15", weather.time)
    }
}
