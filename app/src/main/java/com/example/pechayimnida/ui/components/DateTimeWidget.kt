package com.example.pechayimnida.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pechayimnida.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun DateTimeWidget(
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    val currentDate = remember { getCurrentDate() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getCurrentTime()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date – secondary color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = AgriTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = AgriTextSecondary
            )
        }

        // Time – brand green (AgriGreenDeep)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = AgriGreenDeep,   // green icon
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentTime,
                style = MaterialTheme.typography.titleMedium,
                color = AgriGreenDeep   // green text
            )
        }
    }
}

private fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    return formatter.format(Date())
}

private fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date())
}

@Preview(showBackground = true)
@Composable
private fun DateTimeWidgetPreview() {
    AgriVisionTheme {
        DateTimeWidget(
            modifier = Modifier
                .background(AgriSurface)
                .padding(16.dp)
        )
    }
}