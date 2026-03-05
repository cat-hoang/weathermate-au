package me.hchoang.weather.data.repository

import com.google.gson.Gson
import me.hchoang.weather.data.api.BomApiService
import me.hchoang.weather.data.db.ForecastCacheEntity
import me.hchoang.weather.data.db.HourlyForecastCacheEntity
import me.hchoang.weather.data.db.ObservationCacheEntity
import me.hchoang.weather.data.db.WeatherCacheDao
import me.hchoang.weather.data.dto.ForecastResponseDto
import me.hchoang.weather.data.dto.HourlyForecastResponseDto
import me.hchoang.weather.data.dto.LocationSearchResponseDto
import me.hchoang.weather.data.dto.ObservationResponseDto

class WeatherRepository(
    private val api: BomApiService,
    private val cache: WeatherCacheDao
) {
    private val gson = Gson()

    // ── Location search (no caching needed) ─────────────────────────────────

    suspend fun searchLocations(query: String): Result<LocationSearchResponseDto> =
        runCatching { api.searchLocations(query) }

    // ── Cache reads ───────────────────────────────────────────────────────────

    suspend fun getCachedObservation(geohash: String): ObservationResponseDto? =
        cache.getObservation(geohash)?.let {
            gson.fromJson(it.dataJson, ObservationResponseDto::class.java)
        }

    suspend fun getCachedForecast(geohash: String): ForecastResponseDto? =
        cache.getForecast(geohash)?.let {
            gson.fromJson(it.dataJson, ForecastResponseDto::class.java)
        }

    suspend fun getCachedHourlyForecast(geohash: String): HourlyForecastResponseDto? =
        cache.getHourlyForecast(geohash)?.let {
            gson.fromJson(it.dataJson, HourlyForecastResponseDto::class.java)
        }

    // ── Network fetch + cache write ──────────────────────────────────────────

    /**
     * Fetches observations and the 7-day forecast for [geohash] from the BOM API
     * and persists both responses in the local SQLite cache.
     * Returns a [Result] that is a failure only if **both** requests fail.
     */
    suspend fun fetchAndCache(geohash: String): Result<Unit> {
        val obsResult = runCatching { api.getObservations(geohash) }
        val forecastResult = runCatching { api.getDailyForecast(geohash) }
        // Hourly endpoint requires a 6-character geohash
        val hourlyResult = runCatching { api.getHourlyForecast(geohash.take(6)) }

        obsResult.getOrNull()?.let { dto ->
            cache.insertObservation(
                ObservationCacheEntity(geohash = geohash, dataJson = gson.toJson(dto))
            )
        }

        forecastResult.getOrNull()?.let { dto ->
            cache.insertForecast(
                ForecastCacheEntity(geohash = geohash, dataJson = gson.toJson(dto))
            )
        }

        hourlyResult.getOrNull()?.let { dto ->
            cache.insertHourlyForecast(
                HourlyForecastCacheEntity(geohash = geohash, dataJson = gson.toJson(dto))
            )
        }

        return if (obsResult.isFailure && forecastResult.isFailure) {
            Result.failure(obsResult.exceptionOrNull() ?: forecastResult.exceptionOrNull()!!)
        } else {
            Result.success(Unit)
        }
    }
}
