package com.example.pechayimnida.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.ai.GeminiAnalyzer
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultSheet(
    result: Pair<String, Float>,
    capturedBitmap: Bitmap?,
    onSaveToHistory: (time: String, height: String, status: HealthStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val (className, _) = result
    val initialIsHealthy = className.contains("healthy", ignoreCase = true)
    
    var currentStatus by remember { 
        mutableStateOf(if (initialIsHealthy) HealthStatus.HEALTHY else HealthStatus.DISEASE) 
    }

    var geminiAnalysis by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }

    // User selections
    var selectedTime by remember { mutableStateOf("8:00 AM") }
    var selectedHeight by remember { mutableStateOf("0.5 Meters") }

    val times = listOf("8:00 AM", "12:00 PM", "4:00 PM")
    val heights = listOf("0.5 Meters", "1 Meter", "1.5 Meters")

    val analyzer = remember { GeminiAnalyzer() }

    LaunchedEffect(capturedBitmap, retryCount) {
        if (capturedBitmap != null) {
            isAnalyzing = true
            val analysis = analyzer.analyzePlantHealth(
                bitmap = capturedBitmap,
                cropName = "Pechay",
                detectedDisease = if (initialIsHealthy) null else className
            )
            geminiAnalysis = analysis
            
            // Parse for Moderate/Healthy/Diseased verdict to refine status
            val upperAnalysis = analysis.uppercase()
            when {
                upperAnalysis.contains("VERDICT: MODERATE") || upperAnalysis.contains("VERDICT: [MODERATE]") -> {
                    currentStatus = HealthStatus.WARNING
                }
                upperAnalysis.contains("VERDICT: HEALTHY") || upperAnalysis.contains("VERDICT: [HEALTHY]") -> {
                    currentStatus = HealthStatus.HEALTHY
                }
                upperAnalysis.contains("VERDICT: DISEASED") || upperAnalysis.contains("VERDICT: [DISEASED]") -> {
                    currentStatus = HealthStatus.DISEASE
                }
            }
            
            isAnalyzing = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AgriSurface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        dragHandle = { BottomSheetDefaults.DragHandle(color = AgriOutline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AgriGreenLeafLight.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Psychology,
                    contentDescription = null,
                    tint = AgriGreenDeep,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scan Complete",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = AgriTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Data Collection Section ---
            SelectionGroup(
                title = "What time was this observation made?",
                icon = Icons.Rounded.Schedule,
                options = times,
                selectedOption = selectedTime,
                onOptionSelected = { selectedTime = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SelectionGroup(
                title = "What is the estimated height of the plant?",
                icon = Icons.Rounded.Height,
                options = heights,
                selectedOption = selectedHeight,
                onOptionSelected = { selectedHeight = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Gemini Analysis Section ---
            Text(
                text = "Swipe up for detailed analysis",
                style = MaterialTheme.typography.labelSmall,
                color = AgriTextTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                color = AgriSurfaceAlt,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = AgriGreenLeaf,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI AGRONOMIST REPORT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = AgriGreenLeaf
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAnalyzing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = AgriGreenLeaf,
                            trackColor = AgriOutline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Analyzing image symptoms and organic remedies...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    } else if (geminiAnalysis != null) {
                        val analysisText = geminiAnalysis!!
                        val isError = analysisText.contains("Error", ignoreCase = true) || 
                                     analysisText.contains("failed", ignoreCase = true) || 
                                     analysisText.contains("Network", ignoreCase = true)
                        
                        Column {
                            Text(
                                text = analysisText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isError) StatusCritical else AgriTextPrimary,
                                lineHeight = 22.sp
                            )
                            
                            if (isError) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { retryCount++ },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("RETRY AI ANALYSIS", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriOutline)
                ) {
                    Text("SCAN AGAIN", style = MaterialTheme.typography.labelLarge, color = AgriTextSecondary)
                }

                Button(
                    onClick = { onSaveToHistory(selectedTime, selectedHeight, currentStatus) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep)
                ) {
                    Text("SAVE REPORT", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SelectionGroup(
    title: String,
    icon: ImageVector,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = AgriGreenDeep, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = AgriTextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AgriGreenDeep else AgriSurfaceAlt)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AgriGreenDeep else AgriOutline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else AgriTextSecondary
                    )
                }
            }
        }
    }
}
