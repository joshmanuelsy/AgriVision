package com.example.pechayimnida.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.data.DetectionResult
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionDetailSheet(
    detection: DetectionResult,
    onDismiss: () -> Unit
) {
    val (statusColor, statusBg, statusIcon) = when (detection.status) {
        HealthStatus.HEALTHY -> Triple(StatusHealthy, StatusHealthyBg, Icons.Rounded.CheckCircle)
        HealthStatus.WARNING -> Triple(StatusWarning, StatusWarningBg, Icons.Rounded.Warning)
        HealthStatus.DISEASE -> Triple(StatusCritical, StatusCriticalBg, Icons.Rounded.Error)
    }

    val dateTimeString = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(detection.timestamp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AgriSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AgriOutline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(statusBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Yard,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = detection.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AgriTextPrimary
            )
            
            Text(
                text = dateTimeString,
                style = MaterialTheme.typography.bodyMedium,
                color = AgriTextTertiary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                color = statusBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = detection.statusLabel,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                            Text(
                                text = "Confidence Level: ${detection.confidencePercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    if (detection.observationTime != null || detection.plantHeight != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = statusColor.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (detection.observationTime != null) {
                                DetailInfoItem(
                                    label = "Time",
                                    value = detection.observationTime,
                                    icon = Icons.Rounded.Schedule,
                                    color = statusColor
                                )
                            }
                            if (detection.plantHeight != null) {
                                DetailInfoItem(
                                    label = "Height",
                                    value = detection.plantHeight,
                                    icon = Icons.Rounded.Height,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Note: Detailed AI agronomist report is currently available only immediately after scanning. This record serves as your health history log.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep)
            ) {
                Text("CLOSE", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DetailInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
            Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}
