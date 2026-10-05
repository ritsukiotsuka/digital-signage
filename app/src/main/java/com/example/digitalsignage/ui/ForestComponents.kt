package com.example.digitalsignage.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import com.example.digitalsignage.ui.theme.Cream
import com.example.digitalsignage.ui.theme.Forest
import com.example.digitalsignage.ui.theme.Leaf
import com.example.digitalsignage.ui.theme.Sky
import com.example.digitalsignage.ui.theme.Sun
import java.util.Locale

@Composable
fun ForestBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val transition = rememberInfiniteTransition(label = "forest")
    val breeze by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2_800), RepeatMode.Reverse),
        label = "breeze",
    )
    Box(
        modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Sky.copy(alpha = .55f), Cream))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Sun.copy(alpha = .8f), size.minDimension * .105f, Offset(size.width * .82f, size.height * .12f))
            val ground = Path().apply {
                moveTo(0f, size.height * .86f)
                quadraticTo(size.width * .28f, size.height * .79f, size.width * .54f, size.height * .87f)
                quadraticTo(size.width * .78f, size.height * .93f, size.width, size.height * .84f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(ground, Leaf.copy(alpha = .22f))
            listOf(.08f to .22f, .91f to .31f, .16f to .68f, .86f to .65f).forEachIndexed { index, (x, y) ->
                val center = Offset(size.width * x + breeze * if (index % 2 == 0) 1 else -1, size.height * y)
                drawOval(
                    color = if (index % 2 == 0) Leaf.copy(alpha = .28f) else Forest.copy(alpha = .18f),
                    topLeft = center - Offset(18f, 10f),
                    size = Size(36f, 20f),
                )
            }
        }
        content()
    }
}

fun formatHeight(heightCm: Float): String = String.format(Locale.JAPAN, "%.1f cm", heightCm)
