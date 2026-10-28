package com.smartgas_frontend.incidentpreventionnotification.data.remote

import com.smartgas_frontend.incidentpreventionnotification.data.remote.dto.AlertDto
import com.smartgas_frontend.incidentpreventionnotification.data.remote.dto.NotificationDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertService {
    @GET("alerts")
    suspend fun getAlerts(@Query("accountId") accountId: Int): Response<List<AlertDto>>

    @GET("notifications")
    suspend fun getNotifications(@Query("accountId") accountId: Int): Response<List<NotificationDto>>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") notificationId: Int): Response<NotificationDto>

    @PATCH("notifications/{id}/confirm")
    suspend fun confirmNotification(@Path("id") notificationId: Int): Response<NotificationDto>
}
