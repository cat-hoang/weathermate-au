package me.hchoang.weather.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.hchoang.weather.data.api.RetrofitClient
import me.hchoang.weather.data.repository.WeatherRepository
import me.hchoang.weather.ui.model.LocationUi
import me.hchoang.weather.ui.util.toUi

data class SearchUiState(
    val query: String = "",
    val results: List<LocationUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val repository = WeatherRepository(RetrofitClient.bomApiService)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .filter { it.trim().length >= 2 }
                .distinctUntilChanged()
                .collect { query -> performSearch(query) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query, error = null)
        queryFlow.value = query
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.searchLocations(query)
                .onSuccess { response ->
                    val results = response.data.map { it.toUi() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = results,
                        error = if (results.isEmpty()) "No locations found for \"$query\"." else null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Search failed: ${e.localizedMessage}"
                    )
                }
        }
    }
}
