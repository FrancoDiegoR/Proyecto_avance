package com.smartgas_frontend.incidentpreventionnotification.data.remote.dto

import com.smartgas_frontend.incidentpreventionnotification.domain.model.Alert

data class AlertDto(
    val id: Int?,
    val incidentId: Int?,
    val zoneName: String?,
    val message: String?,
    val description: String?,
    val event: String?,
    val severity: String?,
    val status: String?,
    val createdAt: String?,
    val sentAt: String?,
    val resolvedAt: String?,
    val isResolved: Boolean?
)

fun AlertDto.toAlert() = Alert(
    id = id ?: 0,
    incidentId = incidentId,
    zoneName = zoneName ?: "",
    message = message ?: description ?: event ?: "",
    severity = severity ?: "",
    status = status?.takeIf { it.isNotBlank() } ?: "Active",
    createdAt = createdAt ?: sentAt,
    resolvedAt = resolvedAt,
    isResolved = isResolved == true
)
