package com.smartgas_frontend.paymentmanagement.domain.model

data class Subscription(
    val id: Int,
    val accountId: Int?,
    val planId: Int,
    val planName: String,
    val status: String,
    val startDate: String?,
    val renewalDate: String?,
    val updatedAt: String?,
    val price: Double,
    val maxZones: Int,
    val maxSensors: Int,
    val features: List<String>
)
