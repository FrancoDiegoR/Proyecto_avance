package com.smartgas_frontend.paymentmanagement.data.remote.dto

import com.google.gson.JsonElement
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription
import com.smartgas_frontend.shared.data.remote.asFeatureList
import com.smartgas_frontend.shared.data.remote.asLimit

data class SubscriptionDto(
    val id: Int?,
    val subscriptionId: Int?,
    val accountId: Int?,
    val planId: Int?,
    val planName: String?,
    val currentPlan: String?,
    val status: String?,
    val startDate: String?,
    val endDate: String?,
    val renewalDate: String?,
    val updatedAt: String?,
    val price: Double?,
    val maxZones: JsonElement?,
    val maxSensors: JsonElement?,
    val features: JsonElement?
)

data class ChangePlanRequest(
    val planId: Int
)

fun SubscriptionDto.matchPlan(plans: List<Plan>): Plan? = plans.find { plan ->
    plan.id == planId || plan.name == planName || plan.name == currentPlan
}

fun SubscriptionDto.toSubscription(plans: List<Plan> = emptyList()): Subscription {
    val matchedPlan = matchPlan(plans)

    return Subscription(
        id = id ?: subscriptionId ?: 1,
        accountId = accountId,
        planId = planId ?: matchedPlan?.id ?: 1,
        planName = planName ?: currentPlan ?: matchedPlan?.name ?: "Basic",
        status = status ?: "Active",
        startDate = startDate,
        renewalDate = renewalDate ?: endDate ?: startDate,
        updatedAt = updatedAt,
        price = price ?: matchedPlan?.price ?: 0.0,
        maxZones = maxZones.asLimit() ?: matchedPlan?.maxZones ?: 0,
        maxSensors = maxSensors.asLimit() ?: matchedPlan?.maxSensors ?: 0,
        features = if (features != null) features.asFeatureList() else matchedPlan?.features.orEmpty()
    )
}
