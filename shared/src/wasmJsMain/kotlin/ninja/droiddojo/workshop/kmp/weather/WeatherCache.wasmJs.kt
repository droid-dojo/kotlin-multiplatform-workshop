package ninja.droiddojo.workshop.kmp.weather

// In-memory fallback — swap for WebWorkerSQLiteDriver (sqlite-web) in the bonus task
actual fun createWeatherCache(): WeatherCache = InMemoryWeatherCache()
