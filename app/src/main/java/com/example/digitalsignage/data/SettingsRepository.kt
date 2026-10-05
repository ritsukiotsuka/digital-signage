package com.example.digitalsignage.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "signage_settings")

class SettingsRepository(private val context: Context) {
    val settings: Flow<SignageSettings> = context.settingsDataStore.data.map { preferences ->
        SignageSettings(
            displayBottomHeightCm = preferences[Keys.DISPLAY_BOTTOM_HEIGHT] ?: 75f,
            displayPhysicalHeightCm = preferences[Keys.DISPLAY_PHYSICAL_HEIGHT] ?: 95.2f,
            cameraHeightCm = preferences[Keys.CAMERA_HEIGHT] ?: 178f,
            subjectDistanceCm = preferences[Keys.SUBJECT_DISTANCE] ?: 220f,
            cameraVerticalFovDegrees = preferences[Keys.CAMERA_VERTICAL_FOV] ?: 50f,
            cameraTiltDegrees = preferences[Keys.CAMERA_TILT] ?: -20f,
            heightCorrectionCm = preferences[Keys.HEIGHT_CORRECTION] ?: 0f,
            countdownSeconds = preferences[Keys.COUNTDOWN_SECONDS] ?: 3,
        )
    }

    suspend fun update(settings: SignageSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DISPLAY_BOTTOM_HEIGHT] = settings.displayBottomHeightCm
            preferences[Keys.DISPLAY_PHYSICAL_HEIGHT] = settings.displayPhysicalHeightCm
            preferences[Keys.CAMERA_HEIGHT] = settings.cameraHeightCm
            preferences[Keys.SUBJECT_DISTANCE] = settings.subjectDistanceCm
            preferences[Keys.CAMERA_VERTICAL_FOV] = settings.cameraVerticalFovDegrees
            preferences[Keys.CAMERA_TILT] = settings.cameraTiltDegrees
            preferences[Keys.HEIGHT_CORRECTION] = settings.heightCorrectionCm
            preferences[Keys.COUNTDOWN_SECONDS] = settings.countdownSeconds
        }
    }

    private object Keys {
        val DISPLAY_BOTTOM_HEIGHT = floatPreferencesKey("display_bottom_height_cm")
        val DISPLAY_PHYSICAL_HEIGHT = floatPreferencesKey("display_physical_height_cm")
        val CAMERA_HEIGHT = floatPreferencesKey("camera_height_cm")
        val SUBJECT_DISTANCE = floatPreferencesKey("subject_distance_cm")
        val CAMERA_VERTICAL_FOV = floatPreferencesKey("camera_vertical_fov_degrees")
        val CAMERA_TILT = floatPreferencesKey("camera_tilt_degrees")
        val HEIGHT_CORRECTION = floatPreferencesKey("height_correction_cm")
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
    }
}
