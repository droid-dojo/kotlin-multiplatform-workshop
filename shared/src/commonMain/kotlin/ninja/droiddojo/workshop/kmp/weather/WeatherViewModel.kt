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
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState.value = WeatherUiState.Loading
            uiState.value = try {
                WeatherUiState.Success(repository.currentWeather())
            } catch (e: Exception) {
                WeatherUiState.Error(e.message ?: "Unbekannter Fehler")
            }
        }
    }
}
