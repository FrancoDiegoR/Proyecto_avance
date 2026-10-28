package com.smartgas_frontend.incidentpreventionnotification.domain.model

data class Alert(
    val id: Int,
    val incidentId: Int?,
    val zoneName: String,
    val message: String,
    val severity: String,
    val status: String,
    val createdAt: String?,
    val resolvedAt: String?,
    val isResolved: Boolean
)
