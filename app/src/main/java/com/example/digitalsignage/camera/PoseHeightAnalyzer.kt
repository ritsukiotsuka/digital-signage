package com.example.digitalsignage.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.digitalsignage.data.SignageSettings
import com.example.digitalsignage.measurement.HeightCalculator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.io.Closeable
import java.nio.ByteOrder
import kotlin.math.abs

enum class PoseGuidance {
    NO_PERSON,
    SHOW_FULL_BODY,
    MOVE_TO_CENTER,
    MOVE_CLOSER,
    MOVE_BACK,
    HOLD_STILL,
}

data class PoseFrame(
    val guidance: PoseGuidance,
    val heightCm: Float? = null,
    val estimatedDistanceCm: Float? = null,
    val headY: Float? = null,
    val footY: Float? = null,
)

class PoseHeightAnalyzer(
    private val settings: () -> SignageSettings,
    private val onFrame: (PoseFrame) -> Unit,
) : ImageAnalysis.Analyzer, Closeable {
    private val poseDetector = PoseDetection.getClient(
        AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build(),
    )
    private val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
            .build(),
    )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                if (!hasVisiblePose(pose)) {
                    onFrame(PoseFrame(PoseGuidance.NO_PERSON))
                    imageProxy.close()
                    return@addOnSuccessListener
                }
                segmenter.process(image)
                    .addOnSuccessListener { mask -> onFrame(buildFrame(pose, mask)) }
                    .addOnFailureListener { onFrame(buildFrame(pose, null)) }
                    .addOnCompleteListener { imageProxy.close() }
            }
            .addOnFailureListener {
                onFrame(PoseFrame(PoseGuidance.NO_PERSON))
                imageProxy.close()
            }
    }

    private fun buildFrame(pose: Pose, mask: SegmentationMask?): PoseFrame {
        val imageWidth = mask?.width?.toFloat() ?: inferImageWidth(pose)
        val imageHeight = mask?.height?.toFloat() ?: inferImageHeight(pose)
        if (imageWidth <= 1f || imageHeight <= 1f) return PoseFrame(PoseGuidance.NO_PERSON)

        val shoulders = listOfNotNull(
            pose.visible(PoseLandmark.LEFT_SHOULDER),
            pose.visible(PoseLandmark.RIGHT_SHOULDER),
        )
        val eyes = listOfNotNull(
            pose.visible(PoseLandmark.LEFT_EYE),
            pose.visible(PoseLandmark.RIGHT_EYE),
        )
        val feet = listOfNotNull(
            pose.visible(PoseLandmark.LEFT_HEEL),
            pose.visible(PoseLandmark.RIGHT_HEEL),
            pose.visible(PoseLandmark.LEFT_FOOT_INDEX),
            pose.visible(PoseLandmark.RIGHT_FOOT_INDEX),
            pose.visible(PoseLandmark.LEFT_ANKLE),
            pose.visible(PoseLandmark.RIGHT_ANKLE),
        )
        if (shoulders.size < 2 || eyes.isEmpty() || feet.size < 2) {
            return PoseFrame(PoseGuidance.SHOW_FULL_BODY)
        }

        val centerX = shoulders.map { it.position.x }.average().toFloat() / imageWidth
        if (centerX !in .28f..0.72f) return PoseFrame(PoseGuidance.MOVE_TO_CENTER)

        val shoulderWidth = abs(shoulders[0].position.x - shoulders[1].position.x)
        val fallbackHeadY = (eyes.minOf { it.position.y } - shoulderWidth * .22f).coerceAtLeast(0f)
        val maskHeadY = mask?.findSilhouetteTop(
            centerX = centerX,
            shoulderWidthFraction = shoulderWidth / imageWidth,
            maxYFraction = shoulders.minOf { it.position.y } / imageHeight,
        )
        val headY = (maskHeadY ?: fallbackHeadY / imageHeight).coerceIn(0f, 1f)
        val footY = (feet.maxOf { it.position.y } / imageHeight).coerceIn(0f, 1f)
        if (headY >= footY || footY < .65f || headY > .45f) {
            return PoseFrame(PoseGuidance.SHOW_FULL_BODY)
        }

        val currentSettings = settings()
        val estimatedDistance = HeightCalculator.estimatedDistanceFromFoot(footY, currentSettings)
        val distanceRatio = estimatedDistance?.div(currentSettings.subjectDistanceCm)
        val guidance = when {
            distanceRatio != null && distanceRatio < .72f -> PoseGuidance.MOVE_BACK
            distanceRatio != null && distanceRatio > 1.35f -> PoseGuidance.MOVE_CLOSER
            else -> PoseGuidance.HOLD_STILL
        }
        return PoseFrame(
            guidance = guidance,
            heightCm = HeightCalculator.fromCamera(headY, footY, currentSettings),
            estimatedDistanceCm = estimatedDistance,
            headY = headY,
            footY = footY,
        )
    }

    private fun SegmentationMask.findSilhouetteTop(
        centerX: Float,
        shoulderWidthFraction: Float,
        maxYFraction: Float,
    ): Float? {
        val halfWidth = (shoulderWidthFraction * width * .42f).toInt().coerceAtLeast(5)
        val center = (centerX * width).toInt()
        val startX = (center - halfWidth).coerceIn(0, width - 1)
        val endX = (center + halfWidth).coerceIn(startX + 1, width)
        val endY = (maxYFraction * height).toInt().coerceIn(1, height)
        val values = buffer.duplicate().order(ByteOrder.nativeOrder())
        for (y in 0 until endY) {
            var foregroundPixels = 0
            for (x in startX until endX) {
                if (values.getFloat((y * width + x) * Float.SIZE_BYTES) > .67f) foregroundPixels++
            }
            if (foregroundPixels >= 3) return y.toFloat() / height
        }
        return null
    }

    private fun hasVisiblePose(pose: Pose): Boolean = pose.allPoseLandmarks.count {
        it.inFrameLikelihood >= .55f
    } >= 8

    private fun Pose.visible(type: Int): PoseLandmark? = getPoseLandmark(type)?.takeIf {
        it.inFrameLikelihood >= .55f
    }

    private fun inferImageWidth(pose: Pose): Float = pose.allPoseLandmarks.maxOfOrNull {
        it.position.x
    }?.coerceAtLeast(1f) ?: 1f

    private fun inferImageHeight(pose: Pose): Float = pose.allPoseLandmarks.maxOfOrNull {
        it.position.y
    }?.coerceAtLeast(1f) ?: 1f

    override fun close() {
        poseDetector.close()
        segmenter.close()
    }
}
