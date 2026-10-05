package com.example.digitalsignage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.digitalsignage.data.SignageSettings

@Composable
fun CameraMeasurementScreen(settings: SignageSettings, onBack: () -> Unit) {
    MeasurementPlaceholder("カメラ計測", onBack)
}

@Composable
fun TouchMeasurementScreen(settings: SignageSettings, onBack: () -> Unit) {
    MeasurementPlaceholder("タッチ計測", onBack)
}

@Composable
private fun MeasurementPlaceholder(title: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title)
        Button(onClick = onBack) { Text("戻る") }
    }
}
