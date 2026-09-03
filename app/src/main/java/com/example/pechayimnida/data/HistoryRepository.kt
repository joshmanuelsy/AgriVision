package com.example.pechayimnida.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class HistoryRepository(private val context: Context) {
    private val fileName = "scan_history.json"
    private val historyFile = File(context.filesDir, fileName)
    private val json = Json { ignoreUnknownKeys = true }

    fun saveHistory(history: List<DetectionResult>) {
        try {
            val jsonString = json.encodeToString(history)
            historyFile.writeText(jsonString)
            Log.d("HistoryRepository", "Saved ${history.size} items to $fileName")
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Failed to save history", e)
        }
    }

    fun loadHistory(): List<DetectionResult> {
        if (!historyFile.exists()) return emptyList()
        return try {
            val jsonString = historyFile.readText()
            json.decodeFromString<List<DetectionResult>>(jsonString)
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Failed to load history", e)
            emptyList()
        }
    }
}
