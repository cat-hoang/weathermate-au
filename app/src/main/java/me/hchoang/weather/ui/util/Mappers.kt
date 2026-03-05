package me.hchoang.weather.ui.util

import me.hchoang.weather.data.dto.*
import me.hchoang.weather.ui.model.CurrentWeatherUi
import me.hchoang.weather.ui.model.DayForecastUi
import me.hchoang.weather.ui.model.HourlyForecastUi
import me.hchoang.weather.ui.model.LocationUi
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SYDNEY_ZONE = ZoneId.of("Australia/Sydney")

fun LocationDto.toUi() = LocationUi(
    geohash = geohash,
    name = name,
    state = state ?: "",
    postcode = postcode ?: ""
)

fun ObservationDataDto.toUi(
    locationName: String,
    iconDescriptor: String
): CurrentWeatherUi {
    return CurrentWeatherUi(
        locationName = locationName,
        stationName = station?.name ?: locationName,
        temp = temp ?: 0.0,
        tempFeelsLike = tempFeelsLike ?: temp ?: 0.0,
        humidity = humidity ?: 0,
        windDirection = wind?.direction ?: "—",
        windSpeedKmh = wind?.speedKilometre ?: 0,
        gustSpeedKmh = gust?.speedKilometre ?: 0,
        rainSince9am = rainSince9am ?: 0.0,
        observationTime = "",
        iconDescriptor = iconDescriptor
    )
}

fun ObservationResponseDto.toUi(locationName: String, iconDescriptor: String): CurrentWeatherUi? {
    return data?.toUi(locationName, iconDescriptor)?.copy(
        observationTime = metadata?.observationTime?.formatIsoToLocal() ?: ""
    )
}

fun DailyForecastDto.toUi(): DayForecastUi {
    val parsedDate = date?.parseIsoDate()
    return DayForecastUi(
        date = parsedDate?.format(DateTimeFormatter.ofPattern("d MMM")) ?: date ?: "",
        dayOfWeek = parsedDate?.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)) ?: "",
        tempMax = tempMax,
        tempMin = tempMin,
        shortText = shortText ?: extendedText ?: "—",
        iconDescriptor = iconDescriptor ?: "unknown",
        rainChance = rain?.chance ?: 0,
        rainAmountMin = rain?.amount?.min,
        rainAmountMax = rain?.amount?.max,
        uvCategory = uv?.category?.replaceFirstChar { it.uppercase() } ?: "—",
        uvMaxIndex = uv?.maxIndex,
        sunriseTime = astronomical?.sunriseTime?.formatIsoToLocalTime() ?: "—",
        sunsetTime = astronomical?.sunsetTime?.formatIsoToLocalTime() ?: "—"
    )
}

private fun String.parseIsoDate(): OffsetDateTime? = runCatching {
    OffsetDateTime.parse(this).atZoneSameInstant(SYDNEY_ZONE).toOffsetDateTime()
}.getOrNull()

private fun String.formatIsoToLocal(): String = parseIsoDate()
    ?.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)) ?: this

private fun String.formatIsoToLocalTime(): String = parseIsoDate()
    ?.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)) ?: this

fun HourlyForecastDto.toUi(): HourlyForecastUi {
    val parsedTime = time?.parseIsoDate()
    val formattedTime = parsedTime?.format(DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)) ?: time ?: ""
    return HourlyForecastUi(
        time = formattedTime,
        temp = temp?.toInt() ?: 0,
        iconDescriptor = iconDescriptor ?: "unknown",
        rainChance = rain?.chance ?: 0,
        isNight = isNight ?: false
    )
}

/** Map BOM icon_descriptor to a weather condition label + emoji for display */
fun weatherIcon(descriptor: String): String = when (descriptor.lowercase()) {
    "sunny", "clear" -> "☀️"
    "mostly_sunny", "mostly_clear" -> "🌤️"
    "partly_cloudy" -> "⛅"
    "cloudy", "mostly_cloudy" -> "☁️"
    "hazy", "light_haze", "hazy_sunshine" -> "🌫️"
    "fog", "freezing_fog" -> "🌁"
    "light_shower", "shower", "showers" -> "🌦️"
    "chance_shower_fine", "mostly_sunny_shower" -> "🌦️"
    "drizzle" -> "🌧️"
    "rain", "heavy_shower", "heavy_rain" -> "🌧️"
    "chance_thunderstorm_rain", "thunderstorm" -> "⛈️"
    "chance_thunderstorm_fine", "wind" -> "🌬️"
    "snow", "light_snow" -> "❄️"
    "frost" -> "🥶"
    else -> "🌡️"
}
