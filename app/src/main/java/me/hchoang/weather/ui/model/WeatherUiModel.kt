package me.hchoang.weather.ui.model

data class LocationUi(
    val geohash: String,
    val name: String,
    val state: String,
    val postcode: String
)

data class CurrentWeatherUi(
    val locationName: String,
    val stationName: String,
    val temp: Double,
    val tempFeelsLike: Double,
    val humidity: Int,
    val windDirection: String,
    val windSpeedKmh: Int,
    val gustSpeedKmh: Int,
    val rainSince9am: Double,
    val observationTime: String,
    val iconDescriptor: String  // from today's forecast
)

data class DayForecastUi(
    val date: String,
    val dayOfWeek: String,
    val tempMax: Int?,
    val tempMin: Int?,
    val shortText: String,
    val iconDescriptor: String,
    val rainChance: Int,
    val rainAmountMin: Double?,
    val rainAmountMax: Double?,
    val uvCategory: String,
    val uvMaxIndex: Int?,
    val sunriseTime: String,
    val sunsetTime: String
)

// Pre-defined major Australian cities for quick access
data class PopularCity(
    val geohash: String,
    val name: String,
    val state: String
)

val POPULAR_AU_CITIES = listOf(
    PopularCity("r1r0fsm", "Sydney", "NSW"),
    PopularCity("r1f9652", "Melbourne", "VIC"),
    PopularCity("r7hgdp4", "Brisbane", "QLD"),
)
