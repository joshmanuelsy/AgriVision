package com.example.pechayimnida.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Modern entry screen with staggered entrance animations for an elegant feel.
 */
@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animateContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    // Staggered alpha animations
    val iconAlpha by animateFloatAsState(
        targetValue = if (animateContent) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "icon_alpha"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (animateContent) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200),
        label = "title_alpha"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (animateContent) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "subtitle_alpha"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (animateContent) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 600),
        label = "button_alpha"
    )

    // Vertical slide animations
    val contentOffsetY by animateFloatAsState(
        targetValue = if (animateContent) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "content_offset"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgriBackground)
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Brand mark ---
        Box(
            modifier = Modifier
                .size(128.dp)
                .alpha(iconAlpha)
                .offset(y = contentOffsetY.dp)
                .clip(CircleShape)
                .background(AgriGreenLeafLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Spa,
                contentDescription = null,
                tint = AgriGreenDeep,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- App name ---
        Text(
            text = "AgriVision",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                lineHeight = 54.sp,
                letterSpacing = 1.sp
            ),
            color = AgriTextPrimary,
            modifier = Modifier
                .alpha(titleAlpha)
                .offset(y = (contentOffsetY * 0.8f).dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Tagline ---
        Text(
            text = "AI-powered crop health monitoring",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = AgriTextSecondary,
            modifier = Modifier
                .alpha(subtitleAlpha)
                .offset(y = (contentOffsetY * 0.6f).dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Credits ---
        Text(
            text = "By: STEM - 15",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = AgriTextSecondary,
            modifier = Modifier
                .alpha(subtitleAlpha)
                .offset(y = (contentOffsetY * 0.4f).dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        // --- Single primary action ---
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(60.dp)
                .alpha(buttonAlpha)
                .offset(y = (contentOffsetY * 0.2f).dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgriGreenDeep,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "CONTINUE",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun WelcomeScreenPreview() {
    AgriVisionTheme {
        WelcomeScreen(onContinue = {})
    }
}