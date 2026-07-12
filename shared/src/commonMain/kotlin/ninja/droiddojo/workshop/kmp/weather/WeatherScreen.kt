package ninja.droiddojo.workshop.kmp.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel { WeatherViewModel(WeatherRepository()) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentAlignment = Alignment.Center,
    ) {
        // Exhaustive by design: forget a state and the compiler complains
        when (val current = state) {
            WeatherUiState.Loading -> CircularProgressIndicator()
            is WeatherUiState.Error -> ErrorContent(current.message, onRetry = viewModel::refresh)
            is WeatherUiState.Success -> WeatherContent(current.weather, onRefresh = viewModel::refresh)
        }
    }
}

@Composable
private fun WeatherContent(weather: CurrentWeather, onRefresh: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(weather.condition.emoji, style = MaterialTheme.typography.displayLarge)
        Text("${weather.temperature} °C", style = MaterialTheme.typography.displayMedium)
        Text("Berlin — ${weather.condition.label}", style = MaterialTheme.typography.titleMedium)
        Text("Wind ${weather.windSpeed} km/h", style = MaterialTheme.typography.bodyMedium)
        Text("Stand: ${formatUpdatedAt(weather.time)}", style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRefresh) {
            Text("Aktualisieren")
        }
    }
}

// Tested on desktop, works fine — ship it!
private fun formatUpdatedAt(isoTime: String): String {
    val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
    val formatter = java.text.SimpleDateFormat("HH:mm")
    return formatter.format(parser.parse(isoTime)!!) + " Uhr"
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Das Wetter lässt sich nicht laden.", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRetry) {
            Text("Nochmal versuchen")
        }
    }
}
