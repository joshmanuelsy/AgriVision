package com.example.pechayimnida.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import android.app.Activity

private val AgriLightColorScheme = lightColorScheme(
    primary = AgriGreenDeep,
    onPrimary = Color.White,
    primaryContainer = AgriGreenLeafLight,
    onPrimaryContainer = AgriGreenDeep,
    secondary = AgriGreenLeaf,
    onSecondary = Color.White,
    background = AgriBackground,
    onBackground = AgriTextPrimary,
    surface = AgriSurface,
    onSurface = AgriTextPrimary,
    surfaceVariant = AgriSurfaceAlt,
    onSurfaceVariant = AgriTextSecondary,
    outline = AgriOutline,
    error = StatusCritical,
    onError = Color.White
)

// Consistent corner radius scale used across all cards/buttons in the app
val AgriShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AgriVisionTheme(content: @Composable () -> Unit) {
    val colorScheme = AgriLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val context = view.context
        if (context is Activity) {
            val window = context.window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AgriTypography,
        shapes = AgriShapes,
        content = content
    )
}
