package ninja.droiddojo.workshop.kmp.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
) : ViewModel() {

    // Kotlin 2.4 explicit backing field - mutable inside the ViewModel, read-only StateFlow outside
    val uiState: StateFlow<WeatherUiState>
        field = MutableStateFlow(WeatherUiState.Loading)

    init {
        // Cached data wins: as soon as the database emits, we show it
        viewModelScope.launch {
            repository.observeWeather().collect { cached ->
                if (cached != null) {
                    uiState.value = WeatherUiState.Success(cached)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (uiState.value !is WeatherUiState.Success) {
                uiState.value = WeatherUiState.Loading
            }
            try {
                repository.refresh()
                // no manual state update: the upsert re-emits through observeWeather()
            } catch (e: Exception) {
                // keep stale data on screen; the error state is for cold starts only
                if (uiState.value !is WeatherUiState.Success) {
                    uiState.value = WeatherUiState.Error(e.message ?: "Unbekannter Fehler")
                }
            }
        }
    }
}
