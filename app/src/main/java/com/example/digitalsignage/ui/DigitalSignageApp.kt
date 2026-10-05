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

enum class AppScreen { HOME, CAMERA, TOUCH, SETTINGS }

@Composable
fun DigitalSignageApp(viewModel: AppViewModel = viewModel()) {
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = screen,
        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(220)) },
        label = "screen",
    ) { target ->
        when (target) {
            AppScreen.HOME -> HomeScreen(
                onCameraClick = { screen = AppScreen.CAMERA },
                onTouchClick = { screen = AppScreen.TOUCH },
                onSettingsClick = { screen = AppScreen.SETTINGS },
            )
            AppScreen.CAMERA -> CameraMeasurementScreen(settings, onBack = { screen = AppScreen.HOME })
            AppScreen.TOUCH -> TouchMeasurementScreen(settings, onBack = { screen = AppScreen.HOME })
            AppScreen.SETTINGS -> SettingsScreen(
                settings = settings,
                onSave = {
                    viewModel.saveSettings(it)
                    screen = AppScreen.HOME
                },
                onBack = { screen = AppScreen.HOME },
            )
        }
    }
}
