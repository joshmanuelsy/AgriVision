package com.example.pechayimnida.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)
    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}

@Serializable
enum class HealthStatus { HEALTHY, WARNING, DISEASE }

@Serializable
data class DetectionResult(
    val id: String,
    val title: String,
    val statusLabel: String,
    val status: HealthStatus,
    val confidencePercent: Int,
    @Serializable(with = DateSerializer::class)
    val timestamp: Date = Date(),
    val observationTime: String? = null,
    val plantHeight: String? = null
)

data class CropHealthSummary(
    val scorePercent: Int,
    val healthyPercent: Int = 0,
    val healthyCount: Int = 0,
    val warningPercent: Int = 0,
    val warningCount: Int = 0,
    val diseasePercent: Int = 0,
    val diseaseCount: Int = 0,
    val totalScans: Int = 0,
    val status: HealthStatus,
    val statusLabel: String,
    val summaryText: String,
    val lastScanLabel: String
)
