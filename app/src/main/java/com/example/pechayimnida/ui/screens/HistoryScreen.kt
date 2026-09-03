package com.example.pechayimnida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.data.DetectionResult
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.ui.components.DetectionCard
import com.example.pechayimnida.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyItems: List<DetectionResult>,
    onDeleteItems: (Set<String>) -> Unit,
    onViewDetection: (DetectionResult) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val filteredHistory = remember(searchText, historyItems) {
        if (searchText.isBlank()) {
            historyItems
        } else {
            historyItems.filter {
                it.title.contains(searchText, ignoreCase = true) ||
                        it.statusLabel.contains(searchText, ignoreCase = true)
            }
        }
    }

    val groupedHistory = remember(filteredHistory) {
        filteredHistory.groupBy { detection ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(detection.timestamp)
        }.toList().sortedByDescending { it.first }
    }

    val stats = remember(historyItems) {
        val total = historyItems.size
        val healthy = historyItems.count { it.status == HealthStatus.HEALTHY }
        val healthyRate = if (total > 0) (healthy * 100 / total) else 0
        Triple(total, healthyRate, historyItems.take(5).size) // Simplified "activity"
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectionMode) "Select items" else "Scan History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AgriTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgriSurface,
                    titleContentColor = AgriTextPrimary
                ),
                actions = {
                    if (selectionMode) {
                        ActionPill(
                            text = if (selectedIds.size == filteredHistory.size) "NONE" else "ALL",
                            onClick = {
                                selectedIds = if (selectedIds.size == filteredHistory.size) emptySet() else filteredHistory.map { it.id }.toSet()
                            },
                            color = AgriGreenDeep
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ActionPill(
                            text = "DELETE",
                            onClick = {
                                onDeleteItems(selectedIds)
                                selectedIds = emptySet()
                                selectionMode = false
                            },
                            enabled = selectedIds.isNotEmpty(),
                            color = if (selectedIds.isNotEmpty()) StatusCritical else AgriTextTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ActionPill(
                            text = "CANCEL",
                            onClick = { selectionMode = false; selectedIds = emptySet() },
                            color = AgriTextSecondary
                        )
                    } else if (historyItems.isNotEmpty()) {
                        ActionPill(
                            text = "EDIT",
                            onClick = { selectionMode = true },
                            color = AgriGreenDeep
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Dashboard
            HistoryStatsBar(
                totalScans = stats.first,
                healthyRate = stats.second,
                recentActivity = stats.third
            )

            // Pill-shaped Search
            ModernSearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (groupedHistory.isEmpty()) {
                EmptyHistoryState(isSearch = searchText.isNotEmpty())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedHistory.forEach { (dateKey, detections) ->
                        item {
                            Text(
                                text = formatDateHeader(dateKey),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = AgriTextTertiary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(detections) { detection ->
                            val isSelected = detection.id in selectedIds
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            selectedIds = if (isSelected) selectedIds - detection.id else selectedIds + detection.id
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AgriGreenDeep)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                DetectionCard(
                                    detection = detection,
                                    onClick = {
                                        if (selectionMode) {
                                            selectedIds = if (isSelected) selectedIds - detection.id else selectedIds + detection.id
                                        } else {
                                            onViewDetection(detection)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatsBar(
    totalScans: Int,
    healthyRate: Int,
    recentActivity: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Total Scans",
            value = totalScans.toString(),
            icon = Icons.Rounded.History,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Healthy Rate",
            value = "$healthyRate%",
            icon = Icons.Rounded.Favorite,
            color = StatusHealthy,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Active (7d)",
            value = recentActivity.toString(),
            icon = Icons.Rounded.Timeline,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = AgriGreenDeep,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriOutline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AgriTextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AgriTextSecondary
            )
        }
    }
}

@Composable
private fun ActionPill(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color = AgriGreenDeep
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (enabled) color else AgriTextTertiary
            )
        }
    }
}

@Composable
private fun ModernSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search your history...", color = AgriTextTertiary) },
        leadingIcon = { 
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AgriGreenDeepAlpha,
                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
            ) {
                Text(
                    text = "SEARCH",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                    color = AgriGreenDeep,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        },
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AgriSurface,
            unfocusedContainerColor = AgriSurface,
            focusedBorderColor = AgriGreenDeep,
            unfocusedBorderColor = AgriOutline,
            cursorColor = AgriGreenDeep
        ),
        singleLine = true
    )
}

@Composable
private fun EmptyHistoryState(isSearch: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AgriGreenLeafLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Rounded.SearchOff else Icons.Rounded.PhotoCamera,
                contentDescription = null,
                tint = AgriGreenDeep,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isSearch) "No matching scans" else "No scan history yet",
            style = MaterialTheme.typography.titleLarge,
            color = AgriTextPrimary
        )
        Text(
            text = if (isSearch) "Try a different search term" else "Your crop analysis history will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = AgriTextSecondary,
            modifier = Modifier.padding(horizontal = 40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!isSearch) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* Navigate to Scan */ },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start First Scan")
            }
        }
    }
}

private fun formatDateHeader(dateKey: String): String {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))

    return when (dateKey) {
        today -> "TODAY"
        yesterday -> "YESTERDAY"
        else -> {
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
                formatter.format(date!!).uppercase()
            } catch (e: Exception) {
                dateKey.uppercase()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HistoryScreenPreview() {
    AgriVisionTheme {
        HistoryScreen(
            historyItems = emptyList(),
            onDeleteItems = {}
        )
    }
}
