package com.smartgas_frontend.kitchenmonitoring.domain.model

data class Sensor(
    val id: Int,
    val accountId: Int?,
    val name: String,
    val code: String,
    val type: String,
    val zoneId: Int?,
    val locationDetail: String,
    val status: String,
    val battery: Int,
    val lastReading: String,
    val lastConnected: String?,
    val createdAt: String?,
    val updatedAt: String?
)
