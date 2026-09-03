package com.example.pechayimnida.drone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext

enum class DroneStreamStatus {
    DISCONNECTED,
    CONNECTING,
    STREAMING,
    ERROR
}

class DroneCameraClient(private val context: Context) {
    private val TAG = "DroneCameraClient"
    private val DRONE_IP = "192.168.80.1"
    private val CONTROL_PORT = 3333
    private val VIDEO_PORT = 2224
    private val RECEIVE_BUFFER_SIZE = 65535

    private val _latestFrame = MutableStateFlow<Bitmap?>(null)
    val latestFrame: StateFlow<Bitmap?> = _latestFrame.asStateFlow()

    private val _streamStatus = MutableStateFlow(DroneStreamStatus.DISCONNECTED)
    val streamStatus: StateFlow<DroneStreamStatus> = _streamStatus.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private var clientJob: Job? = null
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var controlSocket: Socket? = null
    private var videoSocket: DatagramSocket? = null
    
    private var droneNetwork: Network? = null

    fun start() {
        if (_streamStatus.value != DroneStreamStatus.DISCONNECTED) return
        
        _streamStatus.value = DroneStreamStatus.CONNECTING
        _errorState.value = null
        
        clientJob = clientScope.launch {
            try {
                findDroneNetwork()
                if (droneNetwork == null) {
                    _errorState.value = "Connect to Wi-Fi: HF_266f8b4f"
                    _streamStatus.value = DroneStreamStatus.ERROR
                    return@launch
                }

                // Initialize Control (TCP)
                controlSocket = droneNetwork!!.socketFactory.createSocket()
                droneNetwork!!.bindSocket(controlSocket)
                controlSocket?.connect(java.net.InetSocketAddress(DRONE_IP, CONTROL_PORT), 5000)
                
                val outputStream = controlSocket!!.getOutputStream()
                val inputStream = controlSocket!!.getInputStream()

                // Initialize Video (UDP)
                videoSocket = DatagramSocket(null)
                videoSocket?.reuseAddress = true
                videoSocket?.bind(java.net.InetSocketAddress(VIDEO_PORT))
                droneNetwork!!.bindSocket(videoSocket)
                videoSocket?.soTimeout = 5000

                // 1. APP_ACCESS
                sendCtpPacket(outputStream, "APP_ACCESS", "{\"op\":\"PUT\",\"param\":{\"ver\":\"52\",\"type\":\"0\"}}")
                drainTcp(inputStream)

                // 2. DATE_TIME
                val dateStr = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                sendCtpPacket(outputStream, "DATE_TIME", "{\"op\":\"PUT\",\"param\":{\"date\":\"$dateStr\"}}")
                drainTcp(inputStream)

                // 3. CTP_KEEP_ALIVE
                sendCtpPacket(outputStream, "CTP_KEEP_ALIVE", "{\"op\":\"PUT\",\"param\":{}}")
                drainTcp(inputStream)

                delay(500)

                // 4. VIDEO_DATE
                sendCtpPacket(outputStream, "VIDEO_DATE", "{\"op\":\"PUT\",\"param\":{\"dat\":\"0\"}}")
                drainTcp(inputStream)

                // 5. PHOTO_QUALITY
                sendCtpPacket(outputStream, "PHOTO_QUALITY", "{\"op\":\"PUT\",\"param\":{\"qua\":\"2\"}}")
                drainTcp(inputStream)

                // 6. OPEN_RT_STREAM
                sendCtpPacket(outputStream, "OPEN_RT_STREAM", "{\"op\":\"PUT\",\"param\":{\"format\":\"0\",\"h\":\"480\",\"w\":\"640\",\"fps\":\"25\"}}")
                drainTcp(inputStream)

                _streamStatus.value = DroneStreamStatus.STREAMING
                Log.d(TAG, "Drone stream opened successfully")

                // Launch Keepalive and Video loops
                launch { keepAliveLoop(outputStream) }
                videoReceiverLoop()

            } catch (e: Exception) {
                Log.e(TAG, "Start failed", e)
                _errorState.value = e.message ?: "Connection failed"
                _streamStatus.value = DroneStreamStatus.ERROR
                stop()
            }
        }
    }

    private suspend fun keepAliveLoop(outputStream: OutputStream) {
        while (coroutineContext.isActive && _streamStatus.value == DroneStreamStatus.STREAMING) {
            delay(10000)
            try {
                sendCtpPacket(outputStream, "CTP_KEEP_ALIVE", "{\"op\":\"PUT\",\"param\":{}}")
                Log.d(TAG, "Keep-alive sent")
            } catch (e: Exception) {
                Log.e(TAG, "Keep-alive failed", e)
                break
            }
        }
    }

    private suspend fun videoReceiverLoop() {
        val buffer = ByteArray(RECEIVE_BUFFER_SIZE)
        val packet = DatagramPacket(buffer, buffer.size)
        var frameCount = 0

        while (coroutineContext.isActive && _streamStatus.value == DroneStreamStatus.STREAMING) {
            try {
                packet.length = buffer.size
                videoSocket?.receive(packet)
                
                val data = packet.data
                val len = packet.length
                
                // Find JPEG Start Marker FF D8
                var offset = -1
                for (i in 0 until len - 1) {
                    if (data[i] == 0xFF.toByte() && data[i+1] == 0xD8.toByte()) {
                        offset = i
                        break
                    }
                }

                if (offset != -1) {
                    val bitmap = BitmapFactory.decodeByteArray(data, offset, len - offset)
                    if (bitmap != null) {
                        if (frameCount == 0) {
                            Log.d(TAG, "First frame received: ${bitmap.width}x${bitmap.height} at offset $offset")
                        }
                        _latestFrame.value = bitmap
                        frameCount++
                    }
                }
            } catch (e: Exception) {
                if (coroutineContext.isActive) Log.e(TAG, "Video receive error", e)
            }
        }
    }

    fun stop() {
        val wasStreaming = _streamStatus.value == DroneStreamStatus.STREAMING
        _streamStatus.value = DroneStreamStatus.DISCONNECTED
        
        clientScope.launch {
            if (wasStreaming) {
                try {
                    controlSocket?.getOutputStream()?.let {
                        sendCtpPacket(it, "CLOSE_RT_STREAM", "{\"op\":\"PUT\",\"param\":{\"status\":\"1\"}}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Close stream packet failed", e)
                }
            }
            
            clientJob?.cancelAndJoin()
            
            try {
                controlSocket?.close()
                videoSocket?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Socket cleanup failed", e)
            }
            
            controlSocket = null
            videoSocket = null
            _latestFrame.value = null
            Log.d(TAG, "Drone client stopped and cleaned up")
        }
    }

    private fun sendCtpPacket(out: OutputStream, topic: String, json: String) {
        val topicBytes = topic.toByteArray(Charsets.US_ASCII)
        val jsonBytes = json.toByteArray(Charsets.UTF_8)
        
        val totalSize = 4 + 2 + topicBytes.size + 4 + jsonBytes.size
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        
        buffer.put("CTP:".toByteArray(Charsets.US_ASCII))
        buffer.putShort(topicBytes.size.toShort())
        buffer.put(topicBytes)
        buffer.putInt(jsonBytes.size)
        buffer.put(jsonBytes)
        
        out.write(buffer.array())
        out.flush()
        Log.d(TAG, "Sent CTP topic: $topic")
    }

    private fun drainTcp(input: InputStream) {
        try {
            if (input.available() > 0) {
                val temp = ByteArray(input.available())
                input.read(temp)
            }
        } catch (e: Exception) {}
    }

    private fun findDroneNetwork() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = cm.allNetworks
        for (network in networks) {
            val caps = cm.getNetworkCapabilities(network)
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                // In a real scenario, we might want to check the SSID if possible, 
                // but usually the active WiFi network is the one we want.
                droneNetwork = network
                Log.d(TAG, "Found Wi-Fi Network: $network")
                return
            }
        }
        droneNetwork = null
    }
}
