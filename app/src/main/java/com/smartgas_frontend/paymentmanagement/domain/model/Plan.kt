package com.smartgas_frontend.paymentmanagement.domain.model

data class Plan(
    val id: Int,
    val name: String,
    val price: Double = 0.0,
    val description: String = "",
    val features: List<String> = emptyList(),
    // Int.MAX_VALUE (UNLIMITED) representa el valor "Unlimited" del backend.
    val maxSensors: Int = 0,
    val maxZones: Int = 0,
    val reportLevel: String = "Basic",
    val status: String = "Active"
)
