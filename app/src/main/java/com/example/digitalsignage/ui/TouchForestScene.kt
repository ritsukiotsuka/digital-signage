package com.example.digitalsignage.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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

@Composable
fun TouchForestScene(modifier: Modifier = Modifier) {
    val animation = rememberInfiniteTransition(label = "touch scene")
    val breeze by animation.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3_200), RepeatMode.Reverse),
        label = "tree breeze",
    )
    val tailWag by animation.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(780), RepeatMode.Reverse),
        label = "giraffe tail",
    )

    Canvas(modifier) {
        // A very shallow ground line gives the characters somewhere to stand while keeping the
        // measurement area visually empty.
        drawRect(
            color = Leaf.copy(alpha = .16f),
            topLeft = Offset(0f, size.height * .94f),
            size = Size(size.width, size.height * .06f),
        )

        drawLargeRightTree(breeze)
        drawGiraffe(tailWag)
    }
}

private fun DrawScope.drawLargeRightTree(breeze: Float) {
    val trunkLeft = size.width * .82f
    val trunkTop = size.height * .33f
    val trunkWidth = size.width * .15f

    val branches = Path().apply {
        moveTo(trunkLeft + trunkWidth * .48f, size.height)
        lineTo(trunkLeft + trunkWidth * .42f, trunkTop)
        moveTo(trunkLeft + trunkWidth * .48f, size.height * .58f)
        quadraticTo(size.width * .76f, size.height * .48f, size.width * .72f + breeze * 3f, size.height * .38f)
        moveTo(trunkLeft + trunkWidth * .55f, size.height * .50f)
        quadraticTo(size.width * .92f, size.height * .39f, size.width * .96f + breeze * 4f, size.height * .29f)
    }
    drawPath(
        path = branches,
        color = Bark.copy(alpha = .84f),
        style = Stroke(width = trunkWidth * .28f, cap = StrokeCap.Round),
    )
    drawRoundRect(
        color = Bark.copy(alpha = .9f),
        topLeft = Offset(trunkLeft, trunkTop),
        size = Size(trunkWidth, size.height - trunkTop + 20f),
        cornerRadius = CornerRadius(trunkWidth * .32f),
    )
    drawRoundRect(
        color = Color(0xFF966448).copy(alpha = .5f),
        topLeft = Offset(trunkLeft + trunkWidth * .18f, trunkTop + 24f),
        size = Size(trunkWidth * .16f, size.height - trunkTop),
        cornerRadius = CornerRadius(trunkWidth * .08f),
    )

    val crownColor = Leaf.copy(alpha = .92f)
    val darkCrown = Forest.copy(alpha = .82f)
    val radius = size.width * .13f
    val sway = breeze * size.width * .006f
    drawCircle(darkCrown, radius * 1.15f, Offset(size.width * .88f + sway, size.height * .23f))
    drawCircle(crownColor, radius, Offset(size.width * .73f + sway, size.height * .27f))
    drawCircle(crownColor, radius * 1.12f, Offset(size.width * 1.00f + sway, size.height * .30f))
    drawCircle(Leaf.copy(alpha = .84f), radius * .94f, Offset(size.width * .79f + sway, size.height * .16f))
    drawCircle(Leaf.copy(alpha = .88f), radius * .88f, Offset(size.width * .96f + sway, size.height * .14f))
    drawCircle(darkCrown, radius * .72f, Offset(size.width * .72f + sway, size.height * .39f))

    // A few leaves stay close to the tree so the center remains clear for the child.
    repeat(6) { index ->
        val x = size.width * (.72f + (index * 7 % 25) / 100f) + sway * (index % 2 + 1)
        val y = size.height * (.12f + (index * 11 % 34) / 100f)
        drawOval(
            color = if (index % 2 == 0) Forest.copy(alpha = .45f) else Leaf.copy(alpha = .6f),
            topLeft = Offset(x, y),
            size = Size(22f + index % 3 * 5f, 13f + index % 2 * 4f),
        )
    }
}

private fun DrawScope.drawGiraffe(tailWag: Float) {
    val scale = size.minDimension / 900f
    val x = 12f * scale
    val ground = size.height * .94f
    val yellow = Color(0xFFF2B94B)
    val muzzle = Color(0xFFFFD982)
    val spot = Color(0xFFA96332)
    val outline = Color(0xFF704329)

    // Legs and hooves.
    listOf(60f, 132f).forEach { legX ->
        drawRoundRect(
            color = yellow,
            topLeft = Offset(x + legX * scale, ground - 118f * scale),
            size = Size(24f * scale, 122f * scale),
            cornerRadius = CornerRadius(12f * scale),
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(x + (legX - 2f) * scale, ground - 12f * scale),
            size = Size(29f * scale, 15f * scale),
            cornerRadius = CornerRadius(7f * scale),
        )
    }

    // Body, long neck, and head face toward the open measurement area.
    drawRoundRect(
        color = yellow,
        topLeft = Offset(x + 32f * scale, ground - 205f * scale),
        size = Size(132f * scale, 102f * scale),
        cornerRadius = CornerRadius(42f * scale),
    )
    drawRoundRect(
        color = yellow,
        topLeft = Offset(x + 125f * scale, ground - 405f * scale),
        size = Size(39f * scale, 245f * scale),
        cornerRadius = CornerRadius(20f * scale),
    )
    drawRoundRect(
        color = yellow,
        topLeft = Offset(x + 123f * scale, ground - 454f * scale),
        size = Size(100f * scale, 61f * scale),
        cornerRadius = CornerRadius(28f * scale),
    )
    drawOval(
        color = muzzle,
        topLeft = Offset(x + 176f * scale, ground - 432f * scale),
        size = Size(62f * scale, 39f * scale),
    )

    // Ears and ossicones.
    drawOval(yellow, Offset(x + 116f * scale, ground - 463f * scale), Size(35f * scale, 18f * scale))
    drawLine(outline, Offset(x + 151f * scale, ground - 450f * scale), Offset(x + 148f * scale, ground - 482f * scale), 7f * scale, StrokeCap.Round)
    drawLine(outline, Offset(x + 181f * scale, ground - 448f * scale), Offset(x + 185f * scale, ground - 479f * scale), 7f * scale, StrokeCap.Round)
    drawCircle(spot, 7f * scale, Offset(x + 148f * scale, ground - 486f * scale))
    drawCircle(spot, 7f * scale, Offset(x + 186f * scale, ground - 483f * scale))

    // Spots stay simple and high-contrast at signage viewing distance.
    listOf(
        57f to 148f, 104f to 177f, 138f to 215f, 145f to 270f,
        143f to 326f, 150f to 373f, 76f to 188f,
    ).forEachIndexed { index, (spotX, fromGround) ->
        drawCircle(
            color = spot,
            radius = (10f + index % 3 * 3f) * scale,
            center = Offset(x + spotX * scale, ground - fromGround * scale),
        )
    }

    // Friendly face.
    drawCircle(outline, 5f * scale, Offset(x + 192f * scale, ground - 436f * scale))
    drawCircle(Color.White, 1.8f * scale, Offset(x + 193f * scale, ground - 438f * scale))
    drawCircle(outline, 2.6f * scale, Offset(x + 220f * scale, ground - 414f * scale))
    drawArc(
        color = outline,
        startAngle = 8f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(x + 196f * scale, ground - 423f * scale),
        size = Size(26f * scale, 18f * scale),
        style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round),
    )

    // A gently wagging tail is the only movement on the left side.
    val tailStart = Offset(x + 38f * scale, ground - 180f * scale)
    val tailEnd = Offset(x + (6f + tailWag * 10f) * scale, ground - 235f * scale)
    drawLine(outline, tailStart, tailEnd, 6f * scale, StrokeCap.Round)
    drawCircle(spot, 10f * scale, tailEnd)
}
