package com.smartgas_frontend.iam.domain.model

import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone

data class Profile(
    val id: Int,
    val profileId: Int?,
    val fullName: String,
    val email: String,
    val role: String,
    val accountType: String,
    val businessName: String,
    val phone: String,
    val district: String,
    val address: String,
    val memberSince: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val planId: Int?,
    val planName: String
)

data class ProfileStats(
    val sensors: List<Sensor> = emptyList(),
    val zones: List<Zone> = emptyList(),
    val activeIncidents: List<Incident> = emptyList()
)

data class AccountActivity(
    val id: String,
    val title: String,
    val detail: String,
    val createdAt: String?
)
