package com.smartgas_frontend.kitchenmonitoring.data.remote.dto

import com.google.gson.JsonElement
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.shared.data.remote.asTextOrNull

data class SensorDto(
    val id: Int?,
    val accountId: Int?,
    val name: String?,
    val code: String?,
    val type: String?,
    val zoneId: Int?,
    val locationDetail: String?,
    val status: String?,
    val battery: Double?,
    val batteryLevel: Double?,
    val lastReading: JsonElement?,
    val lastConnected: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CreateSensorRequest(
    val accountId: Int,
    val zoneId: Int,
    val code: String,
    val name: String,
    val type: String
)

data class UpdateSensorRequest(
    val name: String? = null,
    val code: String? = null,
    val type: String? = null,
    val zoneId: Int? = null,
    val locationDetail: String? = null,
    val status: String? = null
)

fun SensorDto.toSensor() = Sensor(
    id = id ?: 0,
    accountId = accountId,
    name = name ?: "",
    code = code ?: "",
    type = type ?: "",
    zoneId = zoneId,
    locationDetail = locationDetail ?: "",
    status = status ?: "Online",
    battery = (battery ?: batteryLevel ?: 100.0).toInt(),
    lastReading = lastReading.asTextOrNull() ?: "—",
    lastConnected = lastConnected ?: updatedAt ?: createdAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
