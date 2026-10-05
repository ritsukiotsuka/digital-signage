package com.example.digitalsignage.data

data class SignageSettings(
    val displayBottomHeightCm: Float = 75f,
    val displayPhysicalHeightCm: Float = 95.2f,
    val cameraHeightCm: Float = 178f,
    val subjectDistanceCm: Float = 220f,
    val cameraVerticalFovDegrees: Float = 50f,
    val cameraTiltDegrees: Float = 0f,
    val heightCorrectionCm: Float = 0f,
    val countdownSeconds: Int = 3,
)
