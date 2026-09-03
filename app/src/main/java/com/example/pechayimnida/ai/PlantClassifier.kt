package com.example.pechayimnida.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class PlantClassifier(private val context: Context) {

    companion object {
        private const val TAG = "PlantClassifier"
        private const val MODEL_NAME = "plant_model.tflite"
        private const val INPUT_SIZE = 224
        private const val CONFIDENCE_THRESHOLD = 0.5f
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f))
        .build()
    private val labels = listOf(
        "Apple___Apple_scab",
        "Apple___Black_rot",
        "Apple___Cedar_apple_rust",
        "Apple___healthy",
        "Blueberry___healthy",
        "Cherry_(including_sour)___Powdery_mildew",
        "Cherry_(including_sour)___healthy",
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
        "Corn_(maize)___Common_rust",
        "Corn_(maize)___Northern_Leaf_Blight",
        "Corn_(maize)___healthy",
        "Grape___Black_rot",
        "Grape___Esca_(Black_Measles)",
        "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
        "Grape___healthy",
        "Orange___Haunglongbing_(Citrus_greening)",
        "Peach___Bacterial_spot",
        "Peach___healthy",
        "Pepper,_bell___Bacterial_spot",
        "Pepper,_bell___healthy",
        "Potato___Early_blight",
        "Potato___Late_blight",
        "Potato___healthy",
        "Raspberry___healthy",
        "Soybean___healthy",
        "Squash___Powdery_mildew",
        "Strawberry___Leaf_scorch",
        "Strawberry___healthy",
        "Tomato___Bacterial_spot",
        "Tomato___Early_blight",
        "Tomato___Late_blight",
        "Tomato___Leaf_Mold",
        "Tomato___Septoria_leaf_spot",
        "Tomato___Spider_mites Two-spotted_spider_mite",
        "Tomato___Target_Spot",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
        "Tomato___Tomato_mosaic_virus",
        "Tomato___healthy"
    )

    var isReady = false
        private set

    init {
        try {
            val model = FileUtil.loadMappedFile(context, MODEL_NAME)
            val options = Interpreter.Options().apply {
                try {
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                } catch (e: Exception) {
                    setNumThreads(4)
                }
            }
            interpreter = Interpreter(model, options)
            isReady = true
            Log.d(TAG, "✅ Model loaded with ${labels.size} classes")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Model load failed", e)
            isReady = false
        }
    }

    fun classify(bitmap: Bitmap): Pair<String, Float>? {
        val interpreter = interpreter ?: return null
        if (!isReady) return null

        var tensorImage = TensorImage.fromBitmap(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Run inference
        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, labels.size),
            org.tensorflow.lite.DataType.FLOAT32
        )
        interpreter.run(tensorImage.buffer, outputBuffer.buffer)

        // Get best result
        val output = outputBuffer.floatArray
        var maxIdx = 0
        var maxConf = -1f
        for (i in output.indices) {
            if (output[i] > maxConf) {
                maxConf = output[i]
                maxIdx = i
            }
        }

        if (maxConf < CONFIDENCE_THRESHOLD) return null

        val className = labels.getOrElse(maxIdx) { "Unknown" }
        return Pair(className, maxConf)
    }

    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null
    }
}