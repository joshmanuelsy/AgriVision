package com.example.pechayimnida.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.pechayimnida.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AgriMascot(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_idle")
    
    // Bobbing animation
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Blinking logic
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2000, 5000))
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }

    val eyeScaleY by animateFloatAsState(
        targetValue = if (isBlinking) 0.1f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "blinking"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .offset(y = bobbingOffset.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Draw Legs (Feet)
            val footWidth = 6.dp.toPx()
            val footHeight = 4.dp.toPx()
            drawOval(
                color = AgriGreenDeep,
                topLeft = Offset(w * 0.35f - footWidth/2, h * 0.9f),
                size = Size(footWidth, footHeight)
            )
            drawOval(
                color = AgriGreenDeep,
                topLeft = Offset(w * 0.65f - footWidth/2, h * 0.9f),
                size = Size(footWidth, footHeight)
            )

            // Draw Leaf Body (Simplified Pechay shape)
            val leafPath = Path().apply {
                moveTo(w * 0.5f, h * 0.9f)
                cubicTo(w * 0.1f, h * 0.8f, w * 0.05f, h * 0.3f, w * 0.5f, h * 0.1f)
                cubicTo(w * 0.95f, h * 0.3f, w * 0.9f, h * 0.8f, w * 0.5f, h * 0.9f)
                close()
            }
            drawPath(path = leafPath, color = AgriGreenLeaf)
            
            // Leaf detail (vein)
            drawLine(
                color = AgriGreenDeep.copy(alpha = 0.3f),
                start = Offset(w * 0.5f, h * 0.2f),
                end = Offset(w * 0.5f, h * 0.8f),
                strokeWidth = 3.dp.toPx()
            )

            // Draw Arms (Hands)
            val armStroke = 3.dp.toPx()
            // Left arm
            drawArc(
                color = AgriGreenDeep.copy(alpha = 0.6f),
                startAngle = 160f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = Offset(w * 0.1f, h * 0.4f),
                size = Size(w * 0.3f, h * 0.3f),
                style = Stroke(width = armStroke, cap = StrokeCap.Round)
            )
            // Right arm
            drawArc(
                color = AgriGreenDeep.copy(alpha = 0.6f),
                startAngle = -20f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = Offset(w * 0.6f, h * 0.4f),
                size = Size(w * 0.3f, h * 0.3f),
                style = Stroke(width = armStroke, cap = StrokeCap.Round)
            )

            // Eyes
            val eyeRadius = 5.dp.toPx()
            val leftEyeCenter = Offset(w * 0.35f, h * 0.45f)
            val rightEyeCenter = Offset(w * 0.65f, h * 0.45f)

            // Draw Left Eye
            drawOval(
                color = Color.White,
                topLeft = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius * eyeScaleY),
                size = Size(eyeRadius * 2, (eyeRadius * 2) * eyeScaleY)
            )
            if (!isBlinking) {
                drawCircle(color = AgriGreenDeep, radius = eyeRadius * 0.5f, center = leftEyeCenter)
            }

            // Draw Right Eye
            drawOval(
                color = Color.White,
                topLeft = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y - eyeRadius * eyeScaleY),
                size = Size(eyeRadius * 2, (eyeRadius * 2) * eyeScaleY)
            )
            if (!isBlinking) {
                drawCircle(color = AgriGreenDeep, radius = eyeRadius * 0.5f, center = rightEyeCenter)
            }
            
            // Small Blush (Cute factor)
            drawCircle(color = Color.Red.copy(alpha = 0.2f), radius = eyeRadius * 0.8f, center = Offset(w * 0.25f, h * 0.55f))
            drawCircle(color = Color.Red.copy(alpha = 0.2f), radius = eyeRadius * 0.8f, center = Offset(w * 0.75f, h * 0.55f))

            // Smile
            drawArc(
                color = AgriGreenDeep.copy(alpha = 0.8f),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(w * 0.4f, h * 0.48f),
                size = Size(w * 0.2f, h * 0.15f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
