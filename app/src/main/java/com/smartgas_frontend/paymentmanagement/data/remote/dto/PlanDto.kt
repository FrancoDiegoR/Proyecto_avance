package com.smartgas_frontend.paymentmanagement.data.remote.dto

import com.google.gson.JsonElement
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.remote.asFeatureList
import com.smartgas_frontend.shared.data.remote.asLimit

data class PlanDto(
    val id: Int?,
    val name: String?,
    val price: Double?,
    val currency: String?,
    val description: String?,
    val features: JsonElement?,
    val maxSensors: JsonElement?,
    val maxZones: JsonElement?,
    val sensorLimit: JsonElement?,
    val zoneLimit: JsonElement?,
    val reportLevel: String?
)

fun PlanDto.toPlan() = Plan(
    id = id ?: 0,
    name = name ?: "",
    price = price ?: 0.0,
    description = description ?: "",
    features = features.asFeatureList(),
    maxSensors = maxSensors.asLimit() ?: sensorLimit.asLimit() ?: 0,
    maxZones = maxZones.asLimit() ?: zoneLimit.asLimit() ?: 0,
    reportLevel = reportLevel ?: if (name == "Basic") "Basic" else "Advanced"
)
