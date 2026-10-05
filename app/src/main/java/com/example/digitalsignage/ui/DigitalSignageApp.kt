package com.example.digitalsignage.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

enum class AppScreen { CAMERA, TOUCH, SETTINGS }

@Composable
fun DigitalSignageApp(viewModel: AppViewModel = viewModel()) {
    var screen by remember { mutableStateOf(AppScreen.TOUCH) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = screen,
        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(220)) },
        label = "screen",
    ) { target ->
        when (target) {
            AppScreen.CAMERA -> CameraMeasurementScreen(settings, onBack = { screen = AppScreen.TOUCH })
            AppScreen.TOUCH -> TouchMeasurementScreen(
                settings = settings,
                onCameraClick = { screen = AppScreen.CAMERA },
                onSettingsClick = { screen = AppScreen.SETTINGS },
            )
            AppScreen.SETTINGS -> SettingsScreen(
                settings = settings,
                onSave = {
                    viewModel.saveSettings(it)
                    screen = AppScreen.TOUCH
                },
                onBack = { screen = AppScreen.TOUCH },
            )
        }
    }
}
