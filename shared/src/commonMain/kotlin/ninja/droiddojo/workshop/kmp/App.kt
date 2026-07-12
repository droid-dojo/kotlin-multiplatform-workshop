package ninja.droiddojo.workshop.kmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import kmpworkshop.shared.generated.resources.Res
import kmpworkshop.shared.generated.resources.compose_multiplatform
import ninja.droiddojo.workshop.kmp.weather.WeatherApi

private const val BERLIN_LATITUDE = 52.52
private const val BERLIN_LONGITUDE = 13.41

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                var weatherText by remember { mutableStateOf("Lade Wetter…") }
                LaunchedEffect(Unit) {
                    // quick & dirty — proper Loading/Success/Error state comes in exercise 2.1
                    weatherText = try {
                        val weather = WeatherApi().currentWeather(BERLIN_LATITUDE, BERLIN_LONGITUDE)
                        "Berlin: ${weather.temperature} °C, Wind ${weather.windSpeed} km/h"
                    } catch (e: Exception) {
                        "Fehler: ${e.message}"
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                    Text("Zeitzone: ${getPlatform().timeZoneId}")
                    Text(weatherText)
                }
            }
        }
    }
}