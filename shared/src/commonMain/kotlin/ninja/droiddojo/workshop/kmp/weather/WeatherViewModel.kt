package ninja.droiddojo.workshop.kmp.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Cached data wins: as soon as the database emits, we show it
        viewModelScope.launch {
            repository.observeWeather().collect { cached ->
                if (cached != null) {
                    _uiState.value = WeatherUiState.Success(cached)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value !is WeatherUiState.Success) {
                _uiState.value = WeatherUiState.Loading
            }
            try {
                repository.refresh()
                // no manual state update: the upsert re-emits through observeWeather()
            } catch (e: Exception) {
                // keep stale data on screen; the error state is for cold starts only
                if (_uiState.value !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Error(e.message ?: "Unbekannter Fehler")
                }
            }
        }
    }
}
