package me.hchoang.weather.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observation_cache")
data class ObservationCacheEntity(
    @PrimaryKey val geohash: String,
    /** JSON-serialised ObservationResponseDto */
    val dataJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "forecast_cache")
data class ForecastCacheEntity(
    @PrimaryKey val geohash: String,
    /** JSON-serialised ForecastResponseDto */
    val dataJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)
