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
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            _uiState.value = try {
                WeatherUiState.Success(repository.currentWeather())
            } catch (e: Exception) {
                WeatherUiState.Error(e.message ?: "Unbekannter Fehler")
            }
        }
    }
}
