package com.smartgas_frontend.kitchenmonitoring.data.remote.dto

import com.smartgas_frontend.kitchenmonitoring.domain.model.ReadingResult
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading

data class SensorReadingDto(
    val id: Int?,
    val accountId: Int?,
    val sensorId: Int?,
    val sensorCode: String?,
    val zoneId: Int?,
    val zone: ZoneRefDto?,
    val gasValue: Double?,
    val gasLevel: Double?,
    val gas: Double?,
    val temperatureValue: Double?,
    val temperature: Double?,
    val timestamp: String?,
    val createdAt: String?,
    val recordedAt: String?
)

data class ZoneRefDto(
    val id: Int?,
    val name: String?
)

data class CreateSensorReadingRequest(
    val sensorCode: String,
    val gasLevel: Double,
    val temperature: Double
)

data class ReadingResultDto(
    val incidentCreated: Boolean?,
    val severity: String?,
    val incidentSeverity: String?,
    val incidentType: String?,
    val type: String?
)

fun SensorReadingDto.toSensorReading() = SensorReading(
    id = id ?: 0,
    sensorId = sensorId,
    sensorCode = sensorCode,
    zoneId = zoneId ?: zone?.id,
    gasValue = gasValue ?: gasLevel ?: gas,
    temperatureValue = temperatureValue ?: temperature,
    timestamp = createdAt ?: timestamp ?: recordedAt
)

fun ReadingResultDto.toReadingResult() = ReadingResult(
    incidentCreated = incidentCreated == true,
    severity = severity ?: incidentSeverity,
    incidentType = incidentType ?: type
)
