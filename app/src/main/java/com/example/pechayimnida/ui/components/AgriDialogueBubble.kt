package com.example.pechayimnida.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechayimnida.ui.theme.*

@Composable
fun AgriDialogueBubble(
    index: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val facts = listOf(
        "Hi, I'm Leafie of STEM - 15! Tap 'Next' to know more fun facts about Bok Choy!",
        "🥬 Pechay is also known as bok choy or Chinese cabbage. It’s one of the most popular leafy vegetables in the Philippines!",
        "☀️ Pechay grows best with plenty of sunlight, making it a great crop for home gardens and farms.",
        "💧 Pechay needs regular watering, especially during hot weather. Keep the soil moist—but not flooded!",
        "⚡ Pechay can be harvested in as little as 30–45 days, depending on the variety and growing conditions.",
        "🦴 Pechay contains calcium and vitamin K, nutrients that help support healthy bones.",
        "🛡️ Pechay contains vitamin C and other antioxidants that help support the body's natural defenses.",
        "🌱 Despite being a simple leafy vegetable, pechay provides several important vitamins and minerals.",
        "🐛 Caterpillars, aphids, and other pests love munching on pechay too. Regularly checking the leaves can help catch problems early.",
        "🌏 Pechay belongs to the Brassica family—the same plant family as cabbage, broccoli, and cauliflower.",
        "👀 Healthy pechay leaves should generally look fresh, firm, and vibrant green. Yellowing or wilting can be signs of stress."
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dialogue Bubble
        Surface(
            color = AgriGreenLeafLight,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "dialogue_text"
                ) { targetIndex ->
                    if (targetIndex == -1) {
                        Text(
                            text = "Tap me!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AgriGreenDeep
                            )
                        )
                    } else {
                        Column {
                            Text(
                                text = facts.getOrElse(targetIndex) { "" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                    color = AgriGreenDeep,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Prev Button
                                if (targetIndex > 0) {
                                    IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = AgriGreenDeep)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Next/Close Button
                                if (targetIndex < facts.size - 1) {
                                    TextButton(onClick = onNext) {
                                        Text("NEXT", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = AgriGreenDeep)
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AgriGreenDeep)
                                    }
                                } else {
                                    TextButton(onClick = onClose) {
                                        Text("FINISH", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = AgriGreenDeep)
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Always show a small X to close
                                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.Close, contentDescription = null, tint = AgriGreenDeep.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
