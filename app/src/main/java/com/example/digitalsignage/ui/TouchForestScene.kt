package com.example.digitalsignage.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.digitalsignage.ui.theme.Bark
import com.example.digitalsignage.ui.theme.Forest
import com.example.digitalsignage.ui.theme.Leaf
import com.example.digitalsignage.ui.theme.Sun

@Composable
fun TouchForestScene(modifier: Modifier = Modifier) {
    val animation = rememberInfiniteTransition(label = "touch forest")
    val breeze by animation.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_600), RepeatMode.Reverse),
        label = "tree breeze",
    )
    val birdProgress by animation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9_000, easing = LinearEasing)),
        label = "bird flight",
    )
    val wingBeat by animation.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "bird wings",
    )

    Canvas(modifier) {
        drawCircle(
            color = Sun.copy(alpha = .72f),
            radius = size.minDimension * .09f,
            center = Offset(size.width * .78f, size.height * .11f),
        )

        val distantGround = size.height * .86f
        repeat(8) { index ->
            val x = size.width * (-.04f + index * .15f)
            val scale = .55f + (index % 3) * .08f
            drawTree(
                x = x,
                groundY = distantGround + (index % 2) * 18f,
                scale = scale,
                sway = breeze * 3f,
                trunkColor = Bark.copy(alpha = .24f),
                crownColor = Leaf.copy(alpha = .2f),
            )
        }

        drawTree(
            x = size.width * .08f,
            groundY = size.height * 1.02f,
            scale = 1.42f,
            sway = breeze * 7f,
            trunkColor = Bark.copy(alpha = .72f),
            crownColor = Forest.copy(alpha = .62f),
        )
        drawTree(
            x = size.width * .94f,
            groundY = size.height * 1.04f,
            scale = 1.2f,
            sway = breeze * -6f,
            trunkColor = Bark.copy(alpha = .66f),
            crownColor = Leaf.copy(alpha = .58f),
        )

        repeat(15) { index ->
            val x = size.width * ((index * 37 % 100) / 100f)
            val y = size.height * (.78f + (index * 19 % 18) / 100f)
            drawCircle(
                color = if (index % 2 == 0) Leaf.copy(alpha = .34f) else Forest.copy(alpha = .25f),
                radius = 12f + index % 4 * 5f,
                center = Offset(x + breeze * (index % 3), y),
            )
        }

        val flightX = size.width * (-.18f + birdProgress * 1.36f)
        drawBird(Offset(flightX, size.height * .20f + wingBeat * 7f), 1f, wingBeat)
        drawBird(Offset(flightX - size.width * .12f, size.height * .25f - wingBeat * 5f), .72f, -wingBeat)
        drawBird(Offset(flightX - size.width * .22f, size.height * .17f + wingBeat * 4f), .58f, wingBeat)
    }
}

private fun DrawScope.drawTree(
    x: Float,
    groundY: Float,
    scale: Float,
    sway: Float,
    trunkColor: Color,
    crownColor: Color,
) {
    val trunkWidth = 34f * scale
    val trunkHeight = 210f * scale
    drawRoundRect(
        color = trunkColor,
        topLeft = Offset(x - trunkWidth / 2f, groundY - trunkHeight),
        size = Size(trunkWidth, trunkHeight + 30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trunkWidth / 2f),
    )
    val crownY = groundY - trunkHeight
    val radius = 78f * scale
    drawCircle(crownColor, radius, Offset(x + sway, crownY))
    drawCircle(crownColor.copy(alpha = crownColor.alpha * .9f), radius * .82f, Offset(x - radius * .55f + sway, crownY + radius * .28f))
    drawCircle(crownColor.copy(alpha = crownColor.alpha * .92f), radius * .88f, Offset(x + radius * .56f + sway, crownY + radius * .22f))
    drawCircle(crownColor.copy(alpha = crownColor.alpha * .78f), radius * .68f, Offset(x + sway * 1.2f, crownY - radius * .62f))
}

private fun DrawScope.drawBird(center: Offset, scale: Float, wingBeat: Float) {
    val width = 34f * scale
    val lift = (9f + wingBeat * 5f) * scale
    val bird = Path().apply {
        moveTo(center.x - width, center.y)
        quadraticTo(center.x - width * .48f, center.y - lift, center.x, center.y)
        quadraticTo(center.x + width * .48f, center.y - lift, center.x + width, center.y)
    }
    drawPath(
        path = bird,
        color = Forest.copy(alpha = .72f),
        style = Stroke(width = 4.5f * scale, cap = StrokeCap.Round),
    )
}
