package me.hchoang.weather.data.api

import me.hchoang.weather.data.dto.ForecastResponseDto
import me.hchoang.weather.data.dto.LocationSearchResponseDto
import me.hchoang.weather.data.dto.ObservationResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BomApiService {

    @GET("v1/locations")
    suspend fun searchLocations(
        @Query("search") query: String
    ): LocationSearchResponseDto

    @GET("v1/locations/{geohash}/observations")
    suspend fun getObservations(
        @Path("geohash") geohash: String
    ): ObservationResponseDto

    @GET("v1/locations/{geohash}/forecasts/daily")
    suspend fun getDailyForecast(
        @Path("geohash") geohash: String
    ): ForecastResponseDto
}
