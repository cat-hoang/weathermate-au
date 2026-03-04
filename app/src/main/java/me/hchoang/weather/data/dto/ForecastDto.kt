package me.hchoang.weather.data.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    @SerializedName("data") val data: List<DailyForecastDto>?
)

data class DailyForecastDto(
    @SerializedName("date") val date: String?,
    @SerializedName("temp_max") val tempMax: Int?,
    @SerializedName("temp_min") val tempMin: Int?,
    @SerializedName("extended_text") val extendedText: String?,
    @SerializedName("short_text") val shortText: String?,
    @SerializedName("icon_descriptor") val iconDescriptor: String?,
    @SerializedName("rain") val rain: RainDto?,
    @SerializedName("uv") val uv: UvDto?,
    @SerializedName("astronomical") val astronomical: AstronomicalDto?
)

data class RainDto(
    @SerializedName("amount") val amount: RainAmountDto?,
    @SerializedName("chance") val chance: Int?
)

data class RainAmountDto(
    @SerializedName("min") val min: Double?,
    @SerializedName("max") val max: Double?,
    @SerializedName("units") val units: String?
)

data class UvDto(
    @SerializedName("category") val category: String?,
    @SerializedName("max_index") val maxIndex: Int?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?
)

data class AstronomicalDto(
    @SerializedName("sunrise_time") val sunriseTime: String?,
    @SerializedName("sunset_time") val sunsetTime: String?
)
