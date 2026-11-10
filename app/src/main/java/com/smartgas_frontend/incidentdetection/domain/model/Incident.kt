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
        get() = gasLevel ?: GAS_REGEX.find(detectedValue)?.firstNumber()

    /** Temperatura detectada, con el mismo respaldo que [gasReading]. */
    val temperatureReading: Double?
        get() = temperature ?: TEMP_REGEX.find(detectedValue)?.firstNumber()

    private companion object {
        /** Primer grupo con valor ("Gas: 62" o "62 ppm"). */
        fun MatchResult.firstNumber(): Double? =
            groupValues.drop(1).firstOrNull { it.isNotEmpty() }?.toDoubleOrNull()

        val GAS_REGEX = Regex("(?:Gas:\\s*([\\d.]+))|(?:([\\d.]+)\\s*ppm)", RegexOption.IGNORE_CASE)
        val TEMP_REGEX = Regex("(?:Temp(?:erature|eratura)?:\\s*([\\d.]+))|(?:([\\d.]+)\\s*°C)", RegexOption.IGNORE_CASE)
    }
}
