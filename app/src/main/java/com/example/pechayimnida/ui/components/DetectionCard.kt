package com.example.pechayimnida.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.data.DetectionResult
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modernized detection card with better hierarchy and actionable elements.
 */
@Composable
fun DetectionCard(
    detection: DetectionResult,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (statusColor, statusBg, statusIcon) = when (detection.status) {
        HealthStatus.HEALTHY -> Triple(StatusHealthy, StatusHealthyBg, Icons.Rounded.CheckCircle)
        HealthStatus.WARNING -> Triple(StatusWarning, StatusWarningBg, Icons.Rounded.Warning)
        HealthStatus.DISEASE -> Triple(StatusCritical, StatusCriticalBg, Icons.Rounded.Error)
    }

    val timeString = remember(detection.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(detection.timestamp)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leaf indicator with status color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Yard,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = detection.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AgriTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextTertiary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(label = detection.statusLabel, color = statusColor, background = statusBg, icon = statusIcon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${detection.confidencePercent}% confidence",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextSecondary
                    )
                }
            }

            Surface(
                onClick = onClick,
                shape = CircleShape,
                color = AgriGreenDeepAlpha,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VIEW",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = AgriGreenDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    color: Color,
    background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetectionCardPreview() {
    AgriVisionTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DetectionCard(
                detection = DetectionResult(
                    id = "024",
                    title = "Leaf Scan #024",
                    statusLabel = "Healthy",
                    status = HealthStatus.HEALTHY,
                    confidencePercent = 96
                )
            )
            Spacer(modifier = Modifier.height(8.dp)) // reduced spacing
            DetectionCard(
                detection = DetectionResult(
                    id = "023",
                    title = "Leaf Scan #023",
                    statusLabel = "Possible Disease",
                    status = HealthStatus.DISEASE,
                    confidencePercent = 78
                )
            )
        }
    }
}