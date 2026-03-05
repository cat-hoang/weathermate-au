package me.hchoang.weather.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.hchoang.weather.data.api.RetrofitClient
import me.hchoang.weather.data.db.WeatherDatabase
import me.hchoang.weather.data.repository.WeatherRepository
import me.hchoang.weather.ui.model.CurrentWeatherUi
import me.hchoang.weather.ui.model.DayForecastUi
import me.hchoang.weather.ui.model.HourlyForecastUi
import me.hchoang.weather.ui.model.POPULAR_AU_CITIES
import me.hchoang.weather.ui.model.PopularCity
import me.hchoang.weather.ui.util.toUi

data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedCity: PopularCity? = null,
    val currentWeather: CurrentWeatherUi? = null,
    val hourlyForecast: List<HourlyForecastUi> = emptyList(),
    val forecast: List<DayForecastUi> = emptyList(),
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(
        api = RetrofitClient.bomApiService,
        cache = WeatherDatabase.getInstance(application).weatherCacheDao()
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val popularCities: List<PopularCity> = POPULAR_AU_CITIES

    init {
        prefetchAllCities()
    }

    /**
     * On cold start: for each popular city, fetch from the network only if the local
     * cache is missing or older than [WeatherRepository.CACHE_TTL_MS]. Cities with
     * fresh cached data are skipped entirely. Then display the first city from cache.
     */
    private fun prefetchAllCities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Fetch only cities whose cache is absent or stale
            POPULAR_AU_CITIES
                .map { city ->
                    async {
                        if (!repository.isCacheFresh(city.geohash)) {
                            repository.fetchAndCache(city.geohash)
                        }
                    }
                }
                .awaitAll()

            // Render the default city from cache
            displayFromCache(POPULAR_AU_CITIES.first())
        }
    }

    /**
     * Called when the user taps a city chip. Reads from the local cache — no network call.
     * If the cache is somehow empty (first launch failed), falls back to a live fetch.
     */
    fun loadWeather(city: PopularCity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedCity = city,
                error = null
            )
            displayFromCache(city)
        }
    }

    fun onCitySelected(city: PopularCity) = loadWeather(city)

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun displayFromCache(city: PopularCity) {
        val obsDto = repository.getCachedObservation(city.geohash)
        val forecastDto = repository.getCachedForecast(city.geohash)
        val hourlyDto = repository.getCachedHourlyForecast(city.geohash)

        val forecastList = forecastDto?.data?.map { it.toUi() } ?: emptyList()
        val todayIcon = forecastList.firstOrNull()?.iconDescriptor ?: "unknown"
        val currentWeather = obsDto?.toUi(locationName = city.name, iconDescriptor = todayIcon)

        // Filter hourly forecast to next 24 h only
        val hourlyList = hourlyDto?.data
            ?.take(24)
            ?.map { it.toUi() }
            ?: emptyList()

        if (currentWeather == null && forecastList.isEmpty()) {
            // Cache miss — attempt a live fetch as fallback
            val result = repository.fetchAndCache(city.geohash)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedCity = city,
                    error = "Unable to load weather for ${city.name}. Please check your connection."
                )
                return
            }
            // Retry from cache after successful fetch
            displayFromCache(city)
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            selectedCity = city,
            currentWeather = currentWeather,
            hourlyForecast = hourlyList,
            forecast = forecastList,
            error = null
        )
    }
}
