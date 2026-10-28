package com.smartgas_frontend.incidentpreventionnotification.data.di

import com.smartgas_frontend.incidentpreventionnotification.data.remote.AlertService
import com.smartgas_frontend.incidentpreventionnotification.data.repository.AlertRepository
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IncidentPreventionNotificationDataModule {

    fun getAlertService(): AlertService {
        return SharedDataModule.getRetrofit().create(AlertService::class.java)
    }

    fun getAlertRepository(): AlertRepository {
        return AlertRepository(getAlertService())
    }
}
