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
            subjectDistanceCm = 200f,
            cameraVerticalFovDegrees = 60f,
        )

        val height = HeightCalculator.fromCamera(headY = 0.15f, footY = 0.85f, settings)

        assertEquals(161.66f, height, 0.1f)
    }
}
