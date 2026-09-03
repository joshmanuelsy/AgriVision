package com.example.pechayimnida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pechayimnida.ui.components.*
import com.example.pechayimnida.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    historyItems: List<com.example.pechayimnida.data.DetectionResult>,
    onUpdateHistory: (List<com.example.pechayimnida.data.DetectionResult>) -> Unit,
    onNavigateToScan: () -> Unit,
    onViewDetection: (com.example.pechayimnida.data.DetectionResult) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(AgriTab.HOME) }
    
    // Default empty state / calculated stats
    val health = remember(historyItems) {
        if (historyItems.isEmpty()) {
            com.example.pechayimnida.data.CropHealthSummary(
                scorePercent = 0,
                healthyPercent = 0,
                healthyCount = 0,
                warningPercent = 0,
                warningCount = 0,
                diseasePercent = 0,
                diseaseCount = 0,
                totalScans = 0,
                status = com.example.pechayimnida.data.HealthStatus.HEALTHY,
                statusLabel = "No Scans",
                summaryText = "Start a scan to check your crop health.",
                lastScanLabel = "No recent scans"
            )
        } else {
            val total = historyItems.size
            val healthyCount = historyItems.count { it.status == com.example.pechayimnida.data.HealthStatus.HEALTHY }
            val warningCount = historyItems.count { it.status == com.example.pechayimnida.data.HealthStatus.WARNING }
            val diseaseCount = historyItems.count { it.status == com.example.pechayimnida.data.HealthStatus.DISEASE }

            val healthyPct = (healthyCount * 100 / total)
            val warningPct = (warningCount * 100 / total)
            val diseasePct = (diseaseCount * 100 / total)

            val overallStatus = when {
                healthyPct >= 70 -> com.example.pechayimnida.data.HealthStatus.HEALTHY
                diseasePct >= 35 -> com.example.pechayimnida.data.HealthStatus.DISEASE
                else -> com.example.pechayimnida.data.HealthStatus.WARNING
            }

            val overallLabel = when (overallStatus) {
                com.example.pechayimnida.data.HealthStatus.HEALTHY -> "Healthy"
                com.example.pechayimnida.data.HealthStatus.WARNING -> "Moderate"
                com.example.pechayimnida.data.HealthStatus.DISEASE -> "Caution"
            }

            val summaryStr = "Out of $total scans: $healthyCount Healthy, $warningCount Moderate, $diseaseCount Diseased."

            com.example.pechayimnida.data.CropHealthSummary(
                scorePercent = healthyPct,
                healthyPercent = healthyPct,
                healthyCount = healthyCount,
                warningPercent = warningPct,
                warningCount = warningCount,
                diseasePercent = diseasePct,
                diseaseCount = diseaseCount,
                totalScans = total,
                status = overallStatus,
                statusLabel = overallLabel,
                summaryText = summaryStr,
                lastScanLabel = "Based on $total recorded scans"
            )
        }
    }
    
    val recentDetections = remember(historyItems) { historyItems.take(3) }

    var isLoading by remember { mutableStateOf(true) }
    var dialogueIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    Scaffold(
        containerColor = AgriBackground,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { 
                    if (it == AgriTab.SCAN) {
                        onNavigateToScan()
                    } else {
                        selectedTab = it 
                    }
                }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            AgriTab.HOME -> HomeContent(
                innerPadding = innerPadding,
                health = health,
                detections = recentDetections,
                isLoading = isLoading,
                onNavigateToScan = onNavigateToScan,
                onNavigateToHistory = { selectedTab = AgriTab.HISTORY },
                onViewDetection = onViewDetection,
                dialogueIndex = dialogueIndex,
                onMascotClick = { 
                    if (dialogueIndex == -1) dialogueIndex = 0
                },
                onNextDialogue = { dialogueIndex++ },
                onPrevDialogue = { dialogueIndex-- },
                onCloseDialogue = { dialogueIndex = -1 }
            )
            AgriTab.SCAN -> { /* Handled via onNavigateToScan above */ }
            AgriTab.HISTORY -> HistoryScreen(
                historyItems = historyItems,
                onDeleteItems = { idsToDelete ->
                    onUpdateHistory(historyItems.filter { it.id !in idsToDelete })
                },
                onViewDetection = onViewDetection,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HomeContent(
    innerPadding: PaddingValues,
    health: com.example.pechayimnida.data.CropHealthSummary,
    detections: List<com.example.pechayimnida.data.DetectionResult>,
    isLoading: Boolean,
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onViewDetection: (com.example.pechayimnida.data.DetectionResult) -> Unit,
    dialogueIndex: Int,
    onMascotClick: () -> Unit,
    onNextDialogue: () -> Unit,
    onPrevDialogue: () -> Unit,
    onCloseDialogue: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            AppHeader(
                title = "Monitor your crops",
                subtitle = "AI-powered crop health monitoring"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                DateTimeWidget(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))
            }
        }

        item {
            CropHealthCard(
                scorePercent = health.scorePercent,
                status = health.status,
                statusLabel = health.statusLabel,
                summaryText = health.summaryText,
                lastScanLabel = health.lastScanLabel,
                isLoading = isLoading
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    AgriMascot(onClick = onMascotClick)
                    Spacer(modifier = Modifier.width(12.dp))
                    AgriDialogueBubble(
                        index = dialogueIndex,
                        onNext = onNextDialogue,
                        onPrev = onPrevDialogue,
                        onClose = onCloseDialogue,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        item {
            PrimaryScanButton(onClick = onNavigateToScan)
        }

        item {
            AgriTipCard()
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Detections",
                    style = MaterialTheme.typography.titleLarge,
                    color = AgriTextPrimary
                )
                TextButton(
                    onClick = onNavigateToHistory
                ) {
                    Text("See All", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        items(detections) { detection ->
            DetectionCard(
                detection = detection,
                onClick = { onViewDetection(detection) }
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}
