package ninja.droiddojo.workshop.kmp.weather

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class WeatherEntity(
    @PrimaryKey val locationKey: String,
    val temperature: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val time: String,
)

fun WeatherEntity.toDomain(): CurrentWeather = CurrentWeather(
    temperature = temperature,
    windSpeed = windSpeed,
    weatherCode = weatherCode,
    time = time,
)

fun CurrentWeather.toEntity(locationKey: String): WeatherEntity = WeatherEntity(
    locationKey = locationKey,
    temperature = temperature,
    windSpeed = windSpeed,
    weatherCode = weatherCode,
    time = time,
)
