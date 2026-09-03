package com.example.pechayimnida.ui.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pechayimnida.drone.DroneCameraClient
import com.example.pechayimnida.drone.DroneStreamStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScanMode {
    TABLET,
    DRONE
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ScanViewModel"
    private val context = application.applicationContext
    
    val droneClient = DroneCameraClient(context)
    
    private val _scanMode = MutableStateFlow(ScanMode.TABLET)
    val scanMode: StateFlow<ScanMode> = _scanMode.asStateFlow()

    private val _isInternetAvailable = MutableStateFlow(false)
    val isInternetAvailable: StateFlow<Boolean> = _isInternetAvailable.asStateFlow()

    private val _pendingDroneCaptureUri = MutableStateFlow<Uri?>(null)
    val pendingDroneCaptureUri: StateFlow<Uri?> = _pendingDroneCaptureUri.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkInternet()
        }
        override fun onLost(network: Network) {
            checkInternet()
        }
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            checkInternet()
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
        checkInternet()
    }

    fun setScanMode(mode: ScanMode) {
        if (mode == ScanMode.TABLET && _scanMode.value == ScanMode.DRONE) {
            droneClient.stop()
        }
        _scanMode.value = mode
    }

    fun checkInternet() {
        viewModelScope.launch {
            val hasInternet = withContext(Dispatchers.IO) {
                val activeNetwork = connectivityManager.activeNetwork
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            }
            _isInternetAvailable.value = hasInternet
        }
    }

    fun captureDroneFrame(bitmap: Bitmap) {
        viewModelScope.launch {
            _isCapturing.value = true
            try {
                val file = withContext(Dispatchers.IO) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "agrivision_drone_$timeStamp.jpg"
                    val storageDir = context.getExternalFilesDir(null)
                    val imageFile = File(storageDir, fileName)
                    
                    FileOutputStream(imageFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        out.flush()
                    }
                    imageFile
                }
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                _pendingDroneCaptureUri.value = uri
                Log.d(TAG, "Drone capture saved to $uri")
                
                // Stop drone stream after capture
                droneClient.stop()
                
            } catch (e: Exception) {
                Log.e(TAG, "Capture failed", e)
            } finally {
                _isCapturing.value = false
            }
        }
    }

    fun clearPendingCapture() {
        _pendingDroneCaptureUri.value = null
    }

    override fun onCleared() {
        super.onCleared()
        droneClient.stop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
