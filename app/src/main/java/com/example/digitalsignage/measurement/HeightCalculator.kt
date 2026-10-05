package com.example.digitalsignage.measurement

import com.example.digitalsignage.data.SignageSettings
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan

object HeightCalculator {
    fun fromTouch(yPx: Float, canvasHeightPx: Float, settings: SignageSettings): Float {
        if (canvasHeightPx <= 0f) return 0f
        val normalizedFromBottom = 1f - (yPx / canvasHeightPx).coerceIn(0f, 1f)
        return settings.displayBottomHeightCm +
            settings.displayPhysicalHeightCm * normalizedFromBottom +
            settings.heightCorrectionCm
    }

    /**
     * Projects head and floor rays onto the configured subject plane. Coordinates are normalized
     * to the upright analysis image, where 0 is the top and 1 is the bottom.
     */
    fun fromCamera(
        headY: Float,
        footY: Float,
        settings: SignageSettings,
    ): Float {
        val headAngle = rayAngle(headY, settings)
        val footAngle = rayAngle(footY, settings)
        return (settings.subjectDistanceCm * (tan(headAngle) - tan(footAngle)) +
            settings.heightCorrectionCm).coerceAtLeast(0.0).toFloat()
    }

    private fun rayAngle(normalizedY: Float, settings: SignageSettings): Double {
        val fov = settings.cameraVerticalFovDegrees.toRadians()
        val imageRay = atan((0.5 - normalizedY.coerceIn(0f, 1f)) * 2.0 * tan(fov / 2.0))
        return settings.cameraTiltDegrees.toRadians() + imageRay
    }

    private fun Float.toRadians(): Double = this * PI / 180.0
}
