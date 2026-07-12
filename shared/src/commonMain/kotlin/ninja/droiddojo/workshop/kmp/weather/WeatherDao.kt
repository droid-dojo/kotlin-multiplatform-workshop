package ninja.droiddojo.workshop.kmp.weather

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Upsert
    suspend fun upsert(entity: WeatherEntity)

    @Query("SELECT * FROM WeatherEntity WHERE locationKey = :key")
    fun observe(key: String): Flow<WeatherEntity?>
}
