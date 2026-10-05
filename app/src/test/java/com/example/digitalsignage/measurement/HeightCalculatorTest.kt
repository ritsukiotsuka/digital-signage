package com.example.digitalsignage.measurement

import com.example.digitalsignage.data.SignageSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class HeightCalculatorTest {
    @Test
    fun touchMapsTopAndBottomToPhysicalDisplay() {
        val settings = SignageSettings(
            displayBottomHeightCm = 75f,
            displayPhysicalHeightCm = 95f,
        )

        assertEquals(170f, HeightCalculator.fromTouch(0f, 1000f, settings), 0.01f)
        assertEquals(75f, HeightCalculator.fromTouch(1000f, 1000f, settings), 0.01f)
    }

    @Test
    fun correctionIsAppliedToTouchMeasurement() {
        val settings = SignageSettings(
            displayBottomHeightCm = 70f,
            displayPhysicalHeightCm = 100f,
            heightCorrectionCm = 1.5f,
        )

        assertEquals(121.5f, HeightCalculator.fromTouch(500f, 1000f, settings), 0.01f)
    }

    @Test
    fun cameraHeightIsDerivedFromTwoImageRays() {
        val settings = SignageSettings(
            cameraHeightCm = 180f,
            subjectDistanceCm = 200f,
            cameraVerticalFovDegrees = 60f,
            cameraTiltDegrees = -20f,
        )

        // Synthetic rays for a 120 cm subject standing 220 cm from a 180 cm-high camera.
        val height = HeightCalculator.fromCamera(headY = 0.4282f, footY = 0.8031f, settings)

        assertEquals(120f, height, 0.2f)
    }
}
