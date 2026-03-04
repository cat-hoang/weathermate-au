package me.hchoang.weather.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.hchoang.weather.ui.model.CurrentWeatherUi
import me.hchoang.weather.ui.model.DayForecastUi
import me.hchoang.weather.ui.model.PopularCity
import me.hchoang.weather.ui.util.weatherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onCitySelected: (PopularCity) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var slideDirection by remember { mutableIntStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show a non-blocking snackbar whenever there is an error but old data is still visible.
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && uiState.currentWeather != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error!!,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = uiState.selectedCity?.let { "${it.name}, ${it.state}" }
                                ?: "BOM Weather",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search location")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // City quick-select chips
                item {
                    CityChipsRow(
                        cities = viewModel.popularCities,
                        selectedCity = uiState.selectedCity,
                        onCityClick = { city ->
                            val currentIdx = viewModel.popularCities.indexOfFirst {
                                it.geohash == uiState.selectedCity?.geohash
                            }
                            val newIdx = viewModel.popularCities.indexOfFirst {
                                it.geohash == city.geohash
                            }
                            if (newIdx != currentIdx) {
                                slideDirection = if (newIdx > currentIdx) 1 else -1
                            }
                            viewModel.onCitySelected(city)
                            onCitySelected(city)
                        }
                    )
                }

                // Current conditions hero card
                uiState.currentWeather?.let { weather ->
                    item {
                        CurrentWeatherHero(
                            weather = weather,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Current detail stats
                    item {
                        WeatherDetailsGrid(
                            weather = weather,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                // 7-day forecast — only this section slides when the city changes
                item {
                    AnimatedContent(
                        targetState = uiState.forecast,
                        transitionSpec = {
                            (slideInHorizontally(tween(350)) { slideDirection * it } + fadeIn(tween(350)))
                                .togetherWith(
                                    slideOutHorizontally(tween(280)) { -slideDirection * it } + fadeOut(tween(200))
                                )
                        },
                        label = "forecastSlide"
                    ) { forecast ->
                        if (forecast.isNotEmpty()) {
                            Column {
                                Text(
                                    text = "7-Day Forecast",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        start = 16.dp, end = 16.dp,
                                        top = 16.dp, bottom = 4.dp
                                    )
                                )
                                forecast.forEachIndexed { index, day ->
                                    DayForecastRow(
                                        day = day,
                                        isToday = index == 0,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Attribution
                item {
                    Text(
                        text = "Data from Bureau of Meteorology (BOM) Australia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Thin loading bar overlaid at the top while refreshing
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }

            // Full-screen error — only when there is no cached data to show
            if (uiState.error != null && uiState.currentWeather == null && !uiState.isLoading) {
                ErrorView(
                    message = uiState.error!!,
                    onRetry = { uiState.selectedCity?.let { viewModel.loadWeather(it) } },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun CityChipsRow(
    cities: List<PopularCity>,
    selectedCity: PopularCity?,
    onCityClick: (PopularCity) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cities) { city ->
            val isSelected = city.geohash == selectedCity?.geohash
            FilterChip(
                selected = isSelected,
                onClick = { onCityClick(city) },
                label = { Text(city.name, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@Composable
private fun CurrentWeatherHero(
    weather: CurrentWeatherUi,
    modifier: Modifier = Modifier
) {
    val skyGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(skyGradient)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = weatherIcon(weather.iconDescriptor),
                    fontSize = 72.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${weather.temp.toInt()}°C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Feels like ${weather.tempFeelsLike.toInt()}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = weather.stationName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                if (weather.observationTime.isNotBlank()) {
                    Text(
                        text = "Observed at ${weather.observationTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailsGrid(
    weather: CurrentWeatherUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatItem("💧", "Humidity", "${weather.humidity}%")
                StatItem("💨", "Wind", "${weather.windDirection} ${weather.windSpeedKmh} km/h")
                StatItem("🌪️", "Gusts", "${weather.gustSpeedKmh} km/h")
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatItem("🌧️", "Rain since 9am", "${weather.rainSince9am} mm")
            }
        }
    }
}

@Composable
private fun StatItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DayForecastRow(
    day: DayForecastUi,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day name
            Column(modifier = Modifier.width(52.dp)) {
                Text(
                    text = if (isToday) "Today" else day.dayOfWeek,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Icon
            Text(
                text = weatherIcon(day.iconDescriptor),
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Short description
            Text(
                text = day.shortText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // Rain chance
            if (day.rainChance > 0) {
                Text(
                    text = "💧${day.rainChance}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Temps
            Row {
                day.tempMin?.let {
                    Text(
                        text = "${it}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = "  /  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                day.tempMax?.let {
                    Text(
                        text = "${it}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// ── Preview helpers ──────────────────────────────────────────────────────────

private val sampleWeather = CurrentWeatherUi(
    locationName = "Sydney",
    stationName = "Sydney Airport AMO",
    temp = 26.4,
    tempFeelsLike = 27.1,
    humidity = 68,
    windDirection = "NE",
    windSpeedKmh = 19,
    gustSpeedKmh = 31,
    rainSince9am = 0.0,
    observationTime = "2:30 pm",
    iconDescriptor = "partly_cloudy"
)

private val sampleForecastToday = DayForecastUi(
    date = "4 Mar",
    dayOfWeek = "Wed",
    tempMax = 28,
    tempMin = 18,
    shortText = "Partly cloudy",
    iconDescriptor = "partly_cloudy",
    rainChance = 10,
    rainAmountMin = null,
    rainAmountMax = 1.0,
    uvCategory = "Very High",
    uvMaxIndex = 9,
    sunriseTime = "6:34 am",
    sunsetTime = "7:41 pm"
)

private val sampleForecastTomorrow = DayForecastUi(
    date = "5 Mar",
    dayOfWeek = "Thu",
    tempMax = 24,
    tempMin = 16,
    shortText = "Shower or two",
    iconDescriptor = "shower",
    rainChance = 70,
    rainAmountMin = 2.0,
    rainAmountMax = 10.0,
    uvCategory = "Moderate",
    uvMaxIndex = 4,
    sunriseTime = "6:35 am",
    sunsetTime = "7:39 pm"
)

@Preview(showBackground = true, name = "Current Weather Hero")
@Composable
private fun PreviewCurrentWeatherHero() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        CurrentWeatherHero(
            weather = sampleWeather,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Weather Details Grid")
@Composable
private fun PreviewWeatherDetailsGrid() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        WeatherDetailsGrid(
            weather = sampleWeather,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Forecast Row – Today")
@Composable
private fun PreviewForecastRowToday() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        DayForecastRow(
            day = sampleForecastToday,
            isToday = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true, name = "Forecast Row – Regular")
@Composable
private fun PreviewForecastRowRegular() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        DayForecastRow(
            day = sampleForecastTomorrow,
            isToday = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true, name = "City Chips Row")
@Composable
private fun PreviewCityChipsRow() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        CityChipsRow(
            cities = me.hchoang.weather.ui.model.POPULAR_AU_CITIES,
            selectedCity = me.hchoang.weather.ui.model.POPULAR_AU_CITIES.first(),
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error View")
@Composable
private fun PreviewErrorView() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        ErrorView(
            message = "Unable to load weather data. Please check your connection.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Full Home Content", heightDp = 900)
@Composable
private fun PreviewHomeContent() {
    me.hchoang.weather.ui.theme.WeatherTheme {
        Scaffold { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    CityChipsRow(
                        cities = me.hchoang.weather.ui.model.POPULAR_AU_CITIES,
                        selectedCity = me.hchoang.weather.ui.model.POPULAR_AU_CITIES.first(),
                        onCityClick = {}
                    )
                }
                item {
                    CurrentWeatherHero(
                        weather = sampleWeather,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    WeatherDetailsGrid(
                        weather = sampleWeather,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                item {
                    Text(
                        text = "7-Day Forecast",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                }
                items(listOf(sampleForecastToday, sampleForecastTomorrow)) { day ->
                    DayForecastRow(
                        day = day,
                        isToday = day == sampleForecastToday,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
