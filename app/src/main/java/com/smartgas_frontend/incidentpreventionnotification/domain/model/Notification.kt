package com.smartgas_frontend.incidentpreventionnotification.domain.model

data class Notification(
    val id: Int,
    val alertId: Int?,
    val incidentId: Int?,
    val message: String,
    val channel: String,
    val read: Boolean,
    val confirmed: Boolean,
    val createdAt: String?,
    val messageKey: String?,
    val messageParams: Map<String, String>
)
