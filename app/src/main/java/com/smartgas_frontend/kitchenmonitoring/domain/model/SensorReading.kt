package com.smartgas_frontend.kitchenmonitoring.domain.model

data class SensorReading(
    val id: Int,
    val sensorId: Int?,
    val sensorCode: String?,
    val zoneId: Int?,
    val gasValue: Double?,
    val temperatureValue: Double?,
    val timestamp: String?
)

data class ReadingResult(
    val incidentCreated: Boolean,
    val severity: String?,
    val incidentType: String?
)
