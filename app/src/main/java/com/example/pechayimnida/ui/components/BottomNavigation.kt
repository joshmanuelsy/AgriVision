package com.example.pechayimnida.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pechayimnida.ui.theme.*

enum class AgriTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Rounded.Home, Icons.Outlined.Home),
    SCAN("Scan", Icons.Rounded.PhotoCamera, Icons.Outlined.PhotoCamera),
    HISTORY("History", Icons.Rounded.History, Icons.Outlined.History)
    // PROFILE removed
}

@Composable
fun BottomNavigationBar(
    selectedTab: AgriTab,
    onTabSelected: (AgriTab) -> Unit
) {
    NavigationBar(
        containerColor = AgriSurface,
        tonalElevation = 0.dp
    ) {
        AgriTab.values().forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AgriGreenDeep)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label
                        )
                    }
                },
                label = {
                    Text(
                        tab.label,
                        style = if (selected)
                            MaterialTheme.typography.labelLarge
                        else
                            MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AgriGreenDeep,
                    selectedTextColor = AgriGreenDeep,
                    indicatorColor = AgriGreenLeafLight,
                    unselectedIconColor = AgriTextTertiary,
                    unselectedTextColor = AgriTextTertiary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    AgriVisionTheme {
        BottomNavigationBar(selectedTab = AgriTab.HOME, onTabSelected = {})
    }
}