package ninja.droiddojo.workshop.kmp.weather

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import kotlinx.coroutines.Dispatchers

@Database(entities = [WeatherEntity::class], version = 1)
@ConstructedBy(WeatherDatabaseConstructor::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}

// The Room compiler generates the actuals — one per target (no reflection on Native!)
@Suppress("KotlinNoActualForExpect")
expect object WeatherDatabaseConstructor : RoomDatabaseConstructor<WeatherDatabase> {
    override fun initialize(): WeatherDatabase
}

// Shared assembly — the platform-specific part (path + driver) comes in via the builder
fun createDatabase(builder: RoomDatabase.Builder<WeatherDatabase>): WeatherDatabase =
    builder
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
