package com.smartgas_frontend.incidentpreventionnotification.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.incidentpreventionnotification.data.remote.AlertService
import com.smartgas_frontend.incidentpreventionnotification.data.remote.dto.toAlert
import com.smartgas_frontend.incidentpreventionnotification.data.remote.dto.toNotification
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Alert
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import com.smartgas_frontend.shared.utils.dateMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlertRepository(private val service: AlertService) {

    suspend fun getAlerts(accountId: Int): Resource<List<Alert>> = withContext(Dispatchers.IO) {
        safeCall { service.getAlerts(accountId).bodyOrThrow().map { it.toAlert() } }
    }

    suspend fun getNotifications(accountId: Int): Resource<List<Notification>> = withContext(Dispatchers.IO) {
        safeCall {
            service.getNotifications(accountId).bodyOrThrow()
                .map { it.toNotification() }
                .sortedByDescending { dateMillis(it.createdAt) }
        }
    }

    suspend fun markNotificationRead(notificationId: Int): Resource<Notification> = withContext(Dispatchers.IO) {
        safeCall { service.markNotificationRead(notificationId).bodyOrThrow().toNotification() }
    }

    suspend fun confirmNotification(notificationId: Int): Resource<Notification> = withContext(Dispatchers.IO) {
        safeCall { service.confirmNotification(notificationId).bodyOrThrow().toNotification() }
    }
}
