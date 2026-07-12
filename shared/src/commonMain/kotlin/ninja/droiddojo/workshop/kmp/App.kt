package ninja.droiddojo.workshop.kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ninja.droiddojo.workshop.kmp.weather.WeatherScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        WeatherScreen()
    }
}
