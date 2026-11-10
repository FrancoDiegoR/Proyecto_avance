package com.smartgas_frontend.incidentdetection.domain.model

data class Incident(
    val id: Int,
    val code: String,
    val type: String,
    val zoneId: Int?,
    val zoneName: String,
    val sensorId: Int?,
    val sensorCode: String,
    val detectedValue: String,
    val gasLevel: Double?,
    val temperature: Double?,
    val severity: String,
    val status: String,
    val detectedAt: String?,
    val updatedAt: String?,
    val resolvedAt: String? = null
) {
    /** Gas detectado: el campo directo o, si no llega, el que viene dentro de "Gas: 62 ppm". */
    val gasReading: Double?
        get() = gasLevel ?: GAS_REGEX.find(detectedValue)?.groupValues?.get(1)?.toDoubleOrNull()

    /** Temperatura detectada, con el mismo respaldo que [gasReading]. */
    val temperatureReading: Double?
        get() = temperature ?: TEMP_REGEX.find(detectedValue)?.groupValues?.get(1)?.toDoubleOrNull()

    private companion object {
        val GAS_REGEX = Regex("Gas:\\s*([\\d.]+)", RegexOption.IGNORE_CASE)
        val TEMP_REGEX = Regex("Temp(?:erature|eratura)?:\\s*([\\d.]+)", RegexOption.IGNORE_CASE)
    }
}
