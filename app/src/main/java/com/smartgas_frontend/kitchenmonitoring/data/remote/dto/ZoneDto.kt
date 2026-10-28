package com.smartgas_frontend.kitchenmonitoring.data.remote.dto

import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone

data class ZoneDto(
    val id: Int?,
    val accountId: Int?,
    val name: String?,
    val description: String?,
    val status: String?,
    val gasLevel: Double?,
    val temperature: Double?,
    val sensorCount: Int?,
    val riskLevel: String?,
    val lastUpdated: String?,
    val sensitivity: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CreateZoneRequest(
    val accountId: Int,
    val name: String,
    val description: String,
    val sensitivity: String
)

fun ZoneDto.toZone() = Zone(
    id = id ?: 0,
    accountId = accountId,
    name = name ?: "",
    description = description ?: "",
    status = status ?: "Safe",
    gasLevel = gasLevel,
    temperature = temperature,
    sensorCount = sensorCount ?: 0,
    riskLevel = riskLevel ?: "Low",
    lastUpdated = lastUpdated ?: updatedAt ?: createdAt,
    sensitivity = sensitivity ?: "Medium",
    createdAt = createdAt,
    updatedAt = updatedAt
)
