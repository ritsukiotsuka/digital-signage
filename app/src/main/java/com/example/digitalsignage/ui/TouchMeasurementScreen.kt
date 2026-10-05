package com.example.digitalsignage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.digitalsignage.data.SignageSettings
import com.example.digitalsignage.measurement.HeightCalculator
import com.example.digitalsignage.ui.theme.Bark
import com.example.digitalsignage.ui.theme.Coral
import com.example.digitalsignage.ui.theme.Cream
import com.example.digitalsignage.ui.theme.Forest
import com.example.digitalsignage.ui.theme.Leaf
import com.example.digitalsignage.ui.theme.Sky
import com.example.digitalsignage.ui.theme.Sun
import kotlin.math.roundToInt

private enum class TouchStep { INTRO, DRAW, RESULT }

@Composable
fun TouchMeasurementScreen(settings: SignageSettings, onBack: () -> Unit) {
    var step by remember { mutableStateOf(TouchStep.INTRO) }
    val points = remember { mutableStateListOf<Offset>() }
    var canvasHeight by remember { mutableStateOf(0f) }
    var measuredHeight by remember { mutableStateOf<Float?>(null) }
    val haptics = LocalHapticFeedback.current
    val reveal = remember { Animatable(0f) }

    LaunchedEffect(step) {
        if (step == TouchStep.RESULT) {
            reveal.snapTo(0f)
            reveal.animateTo(1f, spring(dampingRatio = .55f, stiffness = Spring.StiffnessLow))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Sky.copy(alpha = .5f), Cream)))
            .pointerInput(step) {
                if (step == TouchStep.DRAW) {
                    detectDragGestures(
                        onDragStart = { points.clear(); points += it },
                        onDrag = { change, _ -> points += change.position },
                        onDragEnd = {
                            val horizontalSpan = (points.maxOfOrNull { it.x } ?: 0f) -
                                (points.minOfOrNull { it.x } ?: 0f)
                            if (points.size >= 4 && horizontalSpan > size.width * .18f) {
                                val medianY = points.map { it.y }.sorted()[points.size / 2]
                                measuredHeight = HeightCalculator.fromTouch(medianY, size.height.toFloat(), settings)
                                canvasHeight = size.height.toFloat()
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                step = TouchStep.RESULT
                            } else {
                                points.clear()
                            }
                        },
                    )
                }
            },
    ) {
        TouchRuler(settings)
        if (points.isNotEmpty()) {
            Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, Coral, style = Stroke(width = 16f, cap = StrokeCap.Round))
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(22.dp).size(58.dp).background(Color.White.copy(.86f), CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "戻る", tint = Forest)
        }

        when (step) {
            TouchStep.INTRO -> IntroCard { step = TouchStep.DRAW }
            TouchStep.DRAW -> DrawInstruction()
            TouchStep.RESULT -> measuredHeight?.let { height ->
                ResultCelebration(
                    height = height,
                    markerY = points.map { it.y }.sorted().getOrNull(points.size / 2) ?: canvasHeight,
                    reveal = reveal.value,
                    onRetry = { points.clear(); measuredHeight = null; step = TouchStep.DRAW },
                    onDone = onBack,
                )
            }
        }
    }
}

@Composable
private fun TouchRuler(settings: SignageSettings) {
    Canvas(Modifier.fillMaxSize()) {
        val min = settings.displayBottomHeightCm
        val max = min + settings.displayPhysicalHeightCm
        var cm = (min / 5f).roundToInt() * 5
        while (cm <= max.roundToInt()) {
            val y = size.height * (1f - (cm - min) / settings.displayPhysicalHeightCm)
            val major = cm % 10 == 0
            drawLine(
                color = if (major) Forest.copy(.6f) else Forest.copy(.28f),
                start = Offset(0f, y),
                end = Offset(if (major) 42.dp.toPx() else 24.dp.toPx(), y),
                strokeWidth = if (major) 4f else 2f,
            )
            cm += 5
        }
        drawLine(Forest.copy(.3f), Offset(12.dp.toPx(), 0f), Offset(12.dp.toPx(), size.height), 3f)
    }
}

@Composable
private fun BoxScope.IntroCard(onStart: () -> Unit) {
    Card(
        modifier = Modifier.align(Alignment.Center).padding(horizontal = 54.dp),
        shape = RoundedCornerShape(38.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .94f)),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 42.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🦒", fontSize = 74.sp)
            Text("せなかを ぴったり！", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Forest)
            Text(
                "モニターに せなかを あわせて\nあたまの てっぺんに\nよこ線を 引いてね",
                modifier = Modifier.padding(vertical = 22.dp),
                textAlign = TextAlign.Center,
                fontSize = 21.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Leaf),
                modifier = Modifier.fillMaxWidth().height(72.dp),
            ) {
                Icon(Icons.Outlined.TouchApp, null, Modifier.size(34.dp))
                Text("やってみる！", Modifier.padding(start = 12.dp), fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun BoxScope.DrawInstruction() {
    Card(
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 102.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Forest.copy(.92f)),
    ) {
        Text(
            "あたまの上に  よこ線を ひこう！  👉",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun BoxScope.ResultCelebration(
    height: Float,
    markerY: Float,
    reveal: Float,
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
    Canvas(Modifier.fillMaxSize()) {
        drawLine(
            Coral,
            Offset(0f, markerY),
            Offset(size.width * reveal, markerY),
            strokeWidth = 14f,
            cap = StrokeCap.Round,
        )
        val colors = listOf(Coral, Sun, Leaf, Sky, Forest)
        repeat(30) { index ->
            val x = ((index * 83) % 100) / 100f * size.width
            val baseY = ((index * 47) % 55) / 100f * size.height
            val y = baseY + (1f - reveal) * -180f
            drawCircle(colors[index % colors.size].copy(alpha = reveal), 5f + index % 4 * 2f, Offset(x, y))
        }
    }
    AnimatedVisibility(
        visible = reveal > .25f,
        enter = fadeIn() + scaleIn(initialScale = .6f),
        modifier = Modifier.align(Alignment.Center),
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 58.dp),
            shape = RoundedCornerShape(42.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(.96f)),
            elevation = CardDefaults.cardElevation(14.dp),
        ) {
            Column(Modifier.padding(38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✨ はかれたよ！ ✨", fontSize = 25.sp, fontWeight = FontWeight.Black, color = Forest)
                Text(formatHeight(height), fontSize = 56.sp, fontWeight = FontWeight.Black, color = Coral)
                Text("きょうの 柱のきず", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Bark)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Leaf)) {
                        Icon(Icons.Outlined.Refresh, null)
                        Text("もういちど", Modifier.padding(start = 6.dp), fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
                        Text("おわる", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
