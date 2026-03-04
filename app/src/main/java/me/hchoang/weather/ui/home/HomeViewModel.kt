package me.hchoang.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.hchoang.weather.data.api.RetrofitClient
import me.hchoang.weather.data.repository.WeatherRepository
import me.hchoang.weather.ui.model.CurrentWeatherUi
import me.hchoang.weather.ui.model.DayForecastUi
import me.hchoang.weather.ui.model.POPULAR_AU_CITIES
import me.hchoang.weather.ui.model.PopularCity
import me.hchoang.weather.ui.util.toUi

data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedCity: PopularCity? = null,
    val currentWeather: CurrentWeatherUi? = null,
    val forecast: List<DayForecastUi> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository = WeatherRepository(RetrofitClient.bomApiService)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val popularCities: List<PopularCity> = POPULAR_AU_CITIES

    init {
        loadWeather(POPULAR_AU_CITIES.first())
    }

    fun loadWeather(city: PopularCity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedCity = city,
                error = null
            )

            val observationsDeferred = async { repository.getObservations(city.geohash) }
            val forecastDeferred = async { repository.getDailyForecast(city.geohash) }

            val observationsResult = observationsDeferred.await()
            val forecastResult = forecastDeferred.await()

            if (observationsResult.isFailure && forecastResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to load weather data. Please check your connection."
                )
                return@launch
            }

            val forecastList = forecastResult.getOrNull()?.data
                ?.map { it.toUi() }
                ?: emptyList()

            val todayIcon = forecastList.firstOrNull()?.iconDescriptor ?: "unknown"

            val currentWeather = observationsResult.getOrNull()
                ?.toUi(locationName = city.name, iconDescriptor = todayIcon)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentWeather = currentWeather,
                forecast = forecastList,
                error = if (currentWeather == null && forecastList.isEmpty())
                    "No weather data available for ${city.name}." else null
            )
        }
    }

    fun onCitySelected(city: PopularCity) {
        loadWeather(city)
    }
}
