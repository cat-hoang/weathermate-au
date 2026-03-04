package me.hchoang.weather.data.dto

import com.google.gson.annotations.SerializedName

data class ObservationResponseDto(
    @SerializedName("data") val data: ObservationDataDto?,
    @SerializedName("metadata") val metadata: ObservationMetadataDto?
)

data class ObservationDataDto(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("temp_feels_like") val tempFeelsLike: Double?,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("gust") val gust: GustDto?,
    @SerializedName("rain_since_9am") val rainSince9am: Double?,
    @SerializedName("humidity") val humidity: Int?,
    @SerializedName("station") val station: StationDto?
)

data class WindDto(
    @SerializedName("speed_kilometre") val speedKilometre: Int?,
    @SerializedName("speed_knot") val speedKnot: Int?,
    @SerializedName("direction") val direction: String?
)

data class GustDto(
    @SerializedName("speed_kilometre") val speedKilometre: Int?,
    @SerializedName("speed_knot") val speedKnot: Int?
)

data class StationDto(
    @SerializedName("bom_id") val bomId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("distance") val distance: Int?
)

data class ObservationMetadataDto(
    @SerializedName("response_timestamp") val responseTimestamp: String?,
    @SerializedName("observation_time") val observationTime: String?
)
