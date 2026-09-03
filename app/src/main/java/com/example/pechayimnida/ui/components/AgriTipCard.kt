package com.example.pechayimnida.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.ui.theme.*

@Composable
fun AgriTipCard(modifier: Modifier = Modifier) {
    val tips = listOf(
        "Rotate Pechay with legumes like beans to naturally replenish nitrogen in the soil.",
        "Water Pechay early in the morning to allow leaves to dry and prevent fungal growth.",
        "Apply organic compost or well-rotted manure to improve soil structure and nutrients.",
        "Check under the leaves regularly for early signs of aphids or Diamondback moths.",
        "Ensure good spacing (about 10-15cm) between plants to improve air circulation."
    )
    
    val randomTip = remember { tips.random() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AgriGreenDeep,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "AGRI-INSIGHT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = randomTip,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )
            }
        }
    }
}
