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
    val updatedAt: String?
)
