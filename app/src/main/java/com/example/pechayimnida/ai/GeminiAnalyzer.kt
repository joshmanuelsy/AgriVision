package com.example.pechayimnida.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.pechayimnida.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAnalyzer {
    // Reverting to previous model preference (No extra generation config)
    private val generativeModel = GenerativeModel(
        modelName = "models/gemini-3.6-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun analyzePlantHealth(
        bitmap: Bitmap,
        cropName: String,
        detectedDisease: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            if (BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY == "unused") {
                return@withContext "Error: API Key is missing or invalid in local.properties"
            }

            // Keep image optimization to prevent timeouts/503s
            val resizedBitmap = resizeBitmap(bitmap, 768)

            val diseaseContext = if (detectedDisease != null) {
                "Our local scanner detected signs of $detectedDisease."
            } else {
                "Our local scanner is unsure about the specific condition."
            }

            // Structured prompt without asterisks or markdown clutter
            val prompt = """
                You are a professional agronomist specializing in organic farming.
                I have a $cropName plant. $diseaseContext
                
                Analyze the provided image and follow this structure EXACTLY:
                
                Verdict: HEALTHY (or MODERATE or DISEASED)
                
                
                Explanation:
                Provide a clean explanation of the visible symptoms followed by 3-4 actionable organic remedies or preventative steps.
                
                CRITICAL FORMATTING RULES:
                1. Output MUST start with "Verdict: " followed by HEALTHY, MODERATE, or DISEASED.
                2. Put EXACTLY TWO blank lines between the Verdict line and the "Explanation:" section header.
                3. Do NOT use any asterisks (*), hash signs (#), bold text markdown (**), or special symbols. Use plain text numbers (1., 2., 3.) for lists.
                4. Keep your response concise, professional, and easy for a farmer to follow.
            """.trimIndent()

            Log.d("GeminiAnalyzer", "Generating content for $cropName using gemini-1.5-flash...")
            val response = generativeModel.generateContent(
                content {
                    image(resizedBitmap)
                    text(prompt)
                }
            )
            
            Log.d("GeminiAnalyzer", "Response received successfully")
            
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }

            val rawText = response.text ?: "AI returned an empty response. Please try capturing a clearer image."
            sanitizeAiResponse(rawText)
        } catch (e: Exception) {
            Log.e("GeminiAnalyzer", "Analysis failed: ${e.message}", e)
            val errorMsg = e.message ?: "Unknown error"
            
            when {
                errorMsg.contains("404") -> "Error 404: Model not found. Please ensure the model name is correct."
                errorMsg.contains("503") -> "Error 503: Servers are currently busy. Please wait a moment and try again."
                errorMsg.contains("403") -> "Error 403: Permission denied. Check your API key."
                errorMsg.contains("429") -> "Error 429: Rate limit exceeded. Please wait a moment and try again."
                errorMsg.contains("Unable to resolve host") -> "Network error: Please check your internet connection."
                errorMsg.contains("safety", ignoreCase = true) -> "Analysis failed: The image was flagged by safety filters."
                else -> "Analysis failed: $errorMsg"
            }
        }
    }

    private fun sanitizeAiResponse(text: String): String {
        if (text.startsWith("Error") || text.startsWith("Analysis failed") || text.startsWith("Network error")) {
            return text
        }
        
        // Remove asterisks, hashes, backticks, and markdown formatting
        var cleaned = text.replace("*", "")
            .replace("#", "")
            .replace("`", "")

        // Ensure proper spacing between Verdict: and Explanation:
        val verdictRegex = Regex("""Verdict:\s*\[?(HEALTHY|MODERATE|DISEASED)]?""", RegexOption.IGNORE_CASE)
        val match = verdictRegex.find(cleaned)
        
        if (match != null) {
            val status = match.groupValues[1].uppercase()
            val afterVerdict = cleaned.substring(match.range.last + 1).trim()
            val explanationText = afterVerdict.replace(Regex("""^Explanation:\s*""", RegexOption.IGNORE_CASE), "").trim()
            
            cleaned = "Verdict: $status\n\n\nExplanation:\n$explanationText"
        }

        return cleaned
    }

    private fun resizeBitmap(source: Bitmap, maxSize: Int): Bitmap {
        val width = source.width
        val height = source.height

        if (width <= maxSize && height <= maxSize) return source

        val aspectRatio: Float = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxSize
            targetHeight = (maxSize / aspectRatio).toInt()
        } else {
            targetHeight = maxSize
            targetWidth = (maxSize * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
