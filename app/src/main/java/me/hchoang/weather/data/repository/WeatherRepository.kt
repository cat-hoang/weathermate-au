package me.hchoang.weather.data.repository

import me.hchoang.weather.data.api.BomApiService
import me.hchoang.weather.data.dto.ForecastResponseDto
import me.hchoang.weather.data.dto.LocationSearchResponseDto
import me.hchoang.weather.data.dto.ObservationResponseDto

class WeatherRepository(private val api: BomApiService) {

    suspend fun searchLocations(query: String): Result<LocationSearchResponseDto> =
        runCatching { api.searchLocations(query) }

    suspend fun getObservations(geohash: String): Result<ObservationResponseDto> =
        runCatching { api.getObservations(geohash) }

    suspend fun getDailyForecast(geohash: String): Result<ForecastResponseDto> =
        runCatching { api.getDailyForecast(geohash) }
}
