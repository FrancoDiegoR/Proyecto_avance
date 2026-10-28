package com.smartgas_frontend.kitchenmonitoring.domain.model

data class Zone(
    val id: Int,
    val accountId: Int?,
    val name: String,
    val description: String,
    val status: String,
    val gasLevel: Double?,
    val temperature: Double?,
    val sensorCount: Int,
    val riskLevel: String,
    val lastUpdated: String?,
    val sensitivity: String,
    val createdAt: String?,
    val updatedAt: String?
)
