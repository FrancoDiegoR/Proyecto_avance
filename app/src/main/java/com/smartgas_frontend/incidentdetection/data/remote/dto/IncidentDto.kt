package com.smartgas_frontend.incidentdetection.data.remote.dto

import com.google.gson.JsonElement
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.shared.data.remote.asTextOrNull

data class IncidentDto(
    val id: Int?,
    val code: String?,
    val type: String?,
    val incidentType: String?,
    val title: String?,
    val zoneId: Int?,
    val zoneName: String?,
    val zone: IncidentZoneDto?,
    val sensorId: Int?,
    val sensorCode: String?,
    val sensor: IncidentSensorDto?,
    val detectedValue: JsonElement?,
    val value: JsonElement?,
    val gasLevel: Double?,
    val gasValue: Double?,
    val temperature: Double?,
    val temperatureValue: Double?,
    val severity: String?,
    val status: String?,
    val detectedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class IncidentZoneDto(
    val id: Int?,
    val name: String?
)

data class IncidentSensorDto(
    val id: Int?,
    val code: String?
)

fun IncidentDto.toIncident(): Incident {
    val incidentId = id ?: 0

    return Incident(
        id = incidentId,
        code = code?.takeIf { it.isNotBlank() } ?: "INC-${incidentId.toString().padStart(3, '0')}",
        type = type ?: incidentType ?: title ?: "",
        zoneId = zoneId,
        zoneName = zoneName?.takeIf { it.isNotBlank() } ?: zone?.name ?: "Zone ${zoneId ?: "—"}",
        sensorId = sensorId,
        sensorCode = sensorCode?.takeIf { it.isNotBlank() } ?: sensor?.code ?: "",
        detectedValue = detectedValue.asTextOrNull() ?: value.asTextOrNull() ?: "—",
        gasLevel = gasLevel ?: gasValue,
        temperature = temperature ?: temperatureValue,
        severity = severity ?: "",
        status = status?.takeIf { it.isNotBlank() } ?: "Active",
        detectedAt = detectedAt ?: createdAt,
        updatedAt = updatedAt
    )
}
