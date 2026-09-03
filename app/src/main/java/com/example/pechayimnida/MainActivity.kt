package com.example.pechayimnida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pechayimnida.data.DetectionResult
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.data.HistoryRepository
import com.example.pechayimnida.ui.components.DetectionDetailSheet
import com.example.pechayimnida.ui.screens.HomeScreen
import com.example.pechayimnida.ui.screens.ScanScreen
import com.example.pechayimnida.ui.screens.SplashScreen
import com.example.pechayimnida.ui.screens.WelcomeScreen
import com.example.pechayimnida.ui.theme.AgriVisionTheme
import kotlinx.coroutines.delay
import java.util.Date
import java.util.UUID

enum class AppScreen {
    SPLASH,
    WELCOME,
    HOME,
    SCAN
}

class MainActivity : ComponentActivity() {
    private val historyRepository by lazy { HistoryRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgriVisionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                    var historyItems by remember { mutableStateOf(emptyList<DetectionResult>()) }
                    var selectedDetection by remember { mutableStateOf<DetectionResult?>(null) }

                    LaunchedEffect(Unit) {
                        historyItems = historyRepository.loadHistory()
                        delay(2800)
                        currentScreen = AppScreen.WELCOME
                    }

                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(durationMillis = 400),
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.SPLASH -> SplashScreen()
                            AppScreen.WELCOME -> WelcomeScreen(
                                onContinue = { currentScreen = AppScreen.HOME }
                            )
                            AppScreen.HOME -> HomeScreen(
                                historyItems = historyItems,
                                onUpdateHistory = { 
                                    historyItems = it
                                    historyRepository.saveHistory(it)
                                },
                                onNavigateToScan = { currentScreen = AppScreen.SCAN },
                                onViewDetection = { selectedDetection = it }
                            )
                            AppScreen.SCAN -> ScanScreen(
                                onBack = { currentScreen = AppScreen.HOME },
                                onSaveResult = { result, obsTime, height, refinedStatus ->
                                    val (_, confidence) = result
                                    
                                    val statusLabel = when (refinedStatus) {
                                        HealthStatus.HEALTHY -> "Healthy"
                                        HealthStatus.WARNING -> "Moderate"
                                        HealthStatus.DISEASE -> "Diseased"
                                    }

                                    val newRecord = DetectionResult(
                                        id = UUID.randomUUID().toString(),
                                        title = "Pechay Scan #${historyItems.size + 1}",
                                        statusLabel = statusLabel,
                                        status = refinedStatus,
                                        confidencePercent = (confidence * 100).toInt(),
                                        timestamp = Date(),
                                        observationTime = obsTime,
                                        plantHeight = height
                                    )
                                    val newHistory = listOf(newRecord) + historyItems
                                    historyItems = newHistory
                                    historyRepository.saveHistory(newHistory)
                                }
                            )
                        }
                    }

                    selectedDetection?.let { detection ->
                        DetectionDetailSheet(
                            detection = detection,
                            onDismiss = { selectedDetection = null }
                        )
                    }
                }
            }
        }
    }
}
