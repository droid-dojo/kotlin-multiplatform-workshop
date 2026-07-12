package ninja.droiddojo.workshop.kmp.weather

enum class WeatherCondition(val emoji: String, val label: String) {
    CLEAR("☀️", "Klar"),
    PARTLY_CLOUDY("🌤️", "Bewölkt"),
    FOG("🌫️", "Nebel"),
    DRIZZLE("🌦️", "Nieselregen"),
    RAIN("🌧️", "Regen"),
    SNOW("❄️", "Schnee"),
    SHOWERS("🌧️", "Schauer"),
    THUNDERSTORM("⛈️", "Gewitter"),
    UNKNOWN("❔", "Unbekannt"),
}

// WMO weather interpretation codes, see open-meteo.com/en/docs
val CurrentWeather.condition: WeatherCondition
    get() = when (weatherCode) {
        0 -> WeatherCondition.CLEAR
        1, 2, 3 -> WeatherCondition.PARTLY_CLOUDY
        45, 48 -> WeatherCondition.FOG
        in 51..57 -> WeatherCondition.DRIZZLE
        in 61..67 -> WeatherCondition.RAIN
        in 71..77 -> WeatherCondition.SNOW
        in 80..82 -> WeatherCondition.SHOWERS
        85, 86 -> WeatherCondition.SNOW
        in 95..99 -> WeatherCondition.THUNDERSTORM
        else -> WeatherCondition.UNKNOWN
    }
