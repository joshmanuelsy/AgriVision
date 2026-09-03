package com.example.pechayimnida.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.ui.theme.*

@Composable
fun CropHealthCard(
    scorePercent: Int,
    status: HealthStatus,
    statusLabel: String,
    summaryText: String,
    lastScanLabel: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val statusColor = when (status) {
        HealthStatus.HEALTHY -> StatusHealthy
        HealthStatus.WARNING -> StatusWarning
        HealthStatus.DISEASE -> StatusCritical
    }
    val statusBg = when (status) {
        HealthStatus.HEALTHY -> StatusHealthyBg
        HealthStatus.WARNING -> StatusWarningBg
        HealthStatus.DISEASE -> StatusCriticalBg
    }

    val animatedScore by animateFloatAsState(
        targetValue = scorePercent.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "score_animation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        } else {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIAGNOSTIC STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            color = AgriTextSecondary
                        )
                    )
                    StatusPill(label = statusLabel, color = statusColor, background = statusBg)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Diagnostic Scale
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${animatedScore.toInt()}%",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 36.sp,
                                color = AgriTextPrimary
                            )
                        )
                        Text(
                            text = "Health Index",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextTertiary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Linear Diagnostic Scale
                    HealthScale(progress = animatedScore / 100f, color = statusColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgriTextSecondary,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = lastScanLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextTertiary
                )
            }
        }
    }
}

@Composable
private fun HealthScale(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(AgriSurfaceAlt)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0.01f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun StatusPill(label: String, color: Color, background: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(AgriSurfaceAlt)
            .clip(RoundedCornerShape(28.dp))
    )
}

@Preview(showBackground = true)
@Composable
private fun CropHealthCardPreview() {
    AgriVisionTheme {
        CropHealthCard(
            scorePercent = 92,
            status = HealthStatus.HEALTHY,
            statusLabel = "Healthy",
            summaryText = "Your latest crop scan shows healthy growth.",
            lastScanLabel = "Last scan · 10 minutes ago",
            modifier = Modifier.padding(16.dp)
        )
    }
}
