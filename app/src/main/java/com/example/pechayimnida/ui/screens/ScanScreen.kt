package com.example.pechayimnida.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.pechayimnida.data.HealthStatus
import com.example.pechayimnida.ai.PlantClassifier
import com.example.pechayimnida.drone.DroneStreamStatus
import com.example.pechayimnida.ui.components.AIStatusBadge
import com.example.pechayimnida.ui.components.ScanResultSheet
import com.example.pechayimnida.ui.theme.*
import com.example.pechayimnida.ui.viewmodels.ScanMode
import com.example.pechayimnida.ui.viewmodels.ScanViewModel
import java.util.concurrent.Executors

private fun ImageProxy.toBitmap(): Bitmap? {
    if (format != ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) return null
    val buffer = planes[0].buffer
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    buffer.rewind()
    bitmap.copyPixelsFromBuffer(buffer)
    return bitmap
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onSaveResult: (Pair<String, Float>, String, String, HealthStatus) -> Unit,
    viewModel: ScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val scanMode by viewModel.scanMode.collectAsStateWithLifecycle()
    val pendingCaptureUri by viewModel.pendingDroneCaptureUri.collectAsStateWithLifecycle()
    val isInternetAvailable by viewModel.isInternetAvailable.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val lifecycleOwner = LocalLifecycleOwner.current
    // Handle Internet Check on Resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkInternet()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Shared state for analysis results (from either camera or drone)
    var analysisBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var classificationResult by remember { mutableStateOf<Pair<String, Float>?>(null) }
    val classifier = remember { PlantClassifier(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (scanMode == ScanMode.TABLET) {
            if (cameraPermissionState.status.isGranted) {
                CameraView(
                    onBack = onBack,
                    classifier = classifier,
                    onResultCaptured = { bitmap, result ->
                        analysisBitmap = bitmap
                        classificationResult = result
                    }
                )
            } else {
                PermissionRequestView(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                    onBack = onBack
                )
            }
        } else {
            DroneView(
                onBack = onBack,
                viewModel = viewModel
            )
        }

        // Freeze Overlay (Visible when analyzing)
        if (analysisBitmap != null) {
            Image(
                bitmap = analysisBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Mode Switcher (Top Overlay)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            color = Color.Black.copy(alpha = 0.4f),
            shape = CircleShape
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                ModeButton(
                    text = "Tablet Camera",
                    isSelected = scanMode == ScanMode.TABLET,
                    onClick = { viewModel.setScanMode(ScanMode.TABLET) }
                )
                ModeButton(
                    text = "Drone",
                    isSelected = scanMode == ScanMode.DRONE,
                    onClick = { viewModel.setScanMode(ScanMode.DRONE) }
                )
            }
        }

        // Pending Capture Confirmation Overlay
        if (pendingCaptureUri != null) {
            PendingCaptureConfirmation(
                uri = pendingCaptureUri!!,
                isInternetAvailable = isInternetAvailable,
                onAnalyze = { bitmap ->
                    analysisBitmap = bitmap
                    classificationResult = classifier.classify(bitmap) ?: Pair("Pechay (Unknown)", 0f)
                    viewModel.clearPendingCapture()
                },
                onRetake = { 
                    viewModel.clearPendingCapture()
                },
                onCancel = {
                    viewModel.clearPendingCapture()
                    onBack()
                },
                onOpenWifi = {
                    openWifiSettings(context)
                }
            )
        }

        if (analysisBitmap != null && classificationResult != null) {
            ScanResultSheet(
                result = classificationResult!!,
                capturedBitmap = analysisBitmap,
                onSaveToHistory = { time, height, status ->
                    onSaveResult(classificationResult!!, time, height, status)
                    viewModel.clearPendingCapture()
                    onBack()
                },
                onDismiss = {
                    analysisBitmap = null
                    classificationResult = null
                    viewModel.clearPendingCapture()
                }
            )
        }
    }
}

@Composable
private fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) AgriGreenDeep else Color.Transparent,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CameraView(
    onBack: () -> Unit,
    classifier: PlantClassifier,
    onResultCaptured: (Bitmap, Pair<String, Float>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    var flashEnabled by remember { mutableStateOf(false) }
    var localClassificationResult by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    if (!classifier.isReady) {
        DetectorErrorView(onBack = onBack)
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
            propagateMinConstraints = false
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { previewView ->
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    try {
                        val bitmap = imageProxy.toBitmap()
                        if (bitmap != null) {
                            val result = classifier.classify(bitmap)
                            if (result != null) {
                                localClassificationResult = result
                            }
                            bitmap.recycle()
                        }
                    } catch (e: Exception) {
                        Log.e("ScanScreen", "Analysis error", e)
                    }
                    imageProxy.close()
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            AIStatusBadge(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 160.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.Rounded.Close, onClick = onBack)
                GlassIconButton(
                    icon = if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    onClick = { flashEnabled = !flashEnabled }
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.Rounded.PhotoLibrary, onClick = {})
                CaptureButton(
                    onClick = {
                        localClassificationResult?.let { result ->
                            previewViewInstance?.bitmap?.let { bmp ->
                                val captured = bmp.copy(bmp.config ?: Bitmap.Config.ARGB_8888, false)
                                onResultCaptured(captured, result)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun DroneView(
    onBack: () -> Unit,
    viewModel: ScanViewModel
) {
    val context = LocalContext.current
    val streamStatus by viewModel.droneClient.streamStatus.collectAsStateWithLifecycle()
    val errorState by viewModel.droneClient.errorState.collectAsStateWithLifecycle()
    val latestFrame by viewModel.droneClient.latestFrame.collectAsStateWithLifecycle()
    val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()
    
    // Logic for Analysing Pending Drone Capture
    val pendingCaptureUri by viewModel.pendingDroneCaptureUri.collectAsStateWithLifecycle()
    val isInternetAvailable by viewModel.isInternetAvailable.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Drone Feed
            if (latestFrame != null) {
                Image(
                    bitmap = latestFrame!!.asImageBitmap(),
                    contentDescription = "Drone Feed",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AirplanemodeActive,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (streamStatus == DroneStreamStatus.CONNECTING) "Connecting to E99..." else "No Drone Feed",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        if (errorState != null) {
                            Text(text = errorState!!, color = StatusCritical, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            AIStatusBadge(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 160.dp)
            )

            // Top controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.Rounded.Close, onClick = onBack)
                
                Surface(
                    color = when(streamStatus) {
                        DroneStreamStatus.STREAMING -> AgriGreenDeep
                        DroneStreamStatus.ERROR -> StatusCritical
                        else -> Color.White.copy(alpha = 0.2f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = streamStatus.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (streamStatus == DroneStreamStatus.DISCONNECTED || streamStatus == DroneStreamStatus.ERROR) {
                    Button(
                        onClick = { viewModel.droneClient.start() },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONNECT")
                    }
                } else {
                    Button(
                        onClick = { viewModel.droneClient.stop() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCritical),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DISCONNECT")
                    }
                }

                CaptureButton(
                    onClick = {
                        latestFrame?.let { bmp ->
                            viewModel.captureDroneFrame(bmp)
                            openWifiSettings(context)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.size(64.dp))
            }

            if (isCapturing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PendingCaptureConfirmation(
    uri: Uri,
    isInternetAvailable: Boolean,
    onAnalyze: (Bitmap) -> Unit,
    onRetake: () -> Unit,
    onCancel: () -> Unit,
    onOpenWifi: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        try {
            val stream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream).also { stream?.close() }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(AgriSurface, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Drone Capture Ready",
                style = MaterialTheme.typography.headlineSmall,
                color = AgriTextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(200.dp)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isInternetAvailable) {
                Text(
                    text = "Connect to an internet Wi-Fi network to analyze this image.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusCritical,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenWifi,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep)
                ) {
                    Text("CHOOSE WI-FI")
                }
            } else {
                Text(
                    text = "Internet connected. Ready for AI analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusHealthy,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { bitmap?.let { onAnalyze(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep)
                ) {
                    Text("ANALYZE NOW")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                    Text("RETAKE", color = AgriTextSecondary)
                }
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("CANCEL", color = StatusCritical)
                }
            }
        }
    }
}

private fun openWifiSettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val intent = Intent(Settings.Panel.ACTION_WIFI)
        context.startActivity(intent)
    } else {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context.startActivity(intent)
    }
}

@Composable
private fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
        modifier = Modifier.size(80.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun PermissionRequestView(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgriBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.headlineMedium,
            color = AgriTextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AgriVision needs camera access to scan and analyze your crops.",
            style = MaterialTheme.typography.bodyMedium,
            color = AgriTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("GRANT PERMISSION", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onBack) {
            Text("CANCEL", color = AgriTextSecondary)
        }
    }
}

@Composable
private fun DetectorErrorView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgriBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI Model Unavailable",
            style = MaterialTheme.typography.headlineMedium,
            color = AgriTextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "The plant disease model couldn't be loaded. Check the .tflite file.",
            style = MaterialTheme.typography.bodyMedium,
            color = AgriTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenDeep),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("GO BACK", style = MaterialTheme.typography.labelLarge)
        }
    }
}
