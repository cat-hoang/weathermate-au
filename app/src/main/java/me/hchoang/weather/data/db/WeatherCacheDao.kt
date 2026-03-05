package me.hchoang.weather.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherCacheDao {

    // ── Observations ────────────────────────────────────────────────────────

    @Query("SELECT * FROM observation_cache WHERE geohash = :geohash")
    suspend fun getObservation(geohash: String): ObservationCacheEntity?

    @Query("SELECT cachedAt FROM observation_cache WHERE geohash = :geohash")
    suspend fun getObservationCachedAt(geohash: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(entity: ObservationCacheEntity)

    // ── Forecasts ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM forecast_cache WHERE geohash = :geohash")
    suspend fun getForecast(geohash: String): ForecastCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(entity: ForecastCacheEntity)

    // ── Hourly forecasts ──────────────────────────────────────────────────────

    @Query("SELECT * FROM hourly_forecast_cache WHERE geohash = :geohash")
    suspend fun getHourlyForecast(geohash: String): HourlyForecastCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(entity: HourlyForecastCacheEntity)
}
