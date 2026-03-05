package me.hchoang.weather.data.dto

import com.google.gson.annotations.SerializedName

data class HourlyForecastResponseDto(
    @SerializedName("data") val data: List<HourlyForecastDto>?
)

data class HourlyForecastDto(
    @SerializedName("time") val time: String?,
    @SerializedName("temp") val temp: Double?,
    @SerializedName("icon_descriptor") val iconDescriptor: String?,
    @SerializedName("rain") val rain: RainDto?,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("relative_humidity") val humidity: Int?,
    @SerializedName("temp_feels_like") val feelsLikeTemp: Double?,
    @SerializedName("is_night") val isNight: Boolean?
)
