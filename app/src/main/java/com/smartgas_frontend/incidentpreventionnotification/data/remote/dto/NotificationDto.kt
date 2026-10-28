package com.smartgas_frontend.incidentpreventionnotification.data.remote.dto

import com.google.gson.JsonElement
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.shared.data.remote.asStringMap

data class NotificationDto(
    val id: Int?,
    val alertId: Int?,
    val incidentId: Int?,
    val message: String?,
    val title: String?,
    val channel: String?,
    val read: Boolean?,
    val isRead: Boolean?,
    val confirmed: Boolean?,
    val isConfirmed: Boolean?,
    val createdAt: String?,
    val sentAt: String?,
    val messageKey: String?,
    val messageParams: JsonElement?
)

fun NotificationDto.toNotification() = Notification(
    id = id ?: 0,
    alertId = alertId,
    incidentId = incidentId,
    message = message?.takeIf { it.isNotBlank() } ?: title?.takeIf { it.isNotBlank() }
        ?: "SmartGas safety notification",
    channel = channel?.takeIf { it.isNotBlank() } ?: "Web",
    read = read ?: isRead ?: false,
    confirmed = confirmed ?: isConfirmed ?: false,
    createdAt = createdAt ?: sentAt,
    messageKey = messageKey,
    messageParams = messageParams.asStringMap()
)
