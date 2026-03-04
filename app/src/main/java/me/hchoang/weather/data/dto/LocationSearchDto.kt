package me.hchoang.weather.data.dto

import com.google.gson.annotations.SerializedName

data class LocationSearchResponseDto(
    @SerializedName("data") val data: List<LocationDto>
)

data class LocationDto(
    @SerializedName("geohash") val geohash: String,
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("state") val state: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
