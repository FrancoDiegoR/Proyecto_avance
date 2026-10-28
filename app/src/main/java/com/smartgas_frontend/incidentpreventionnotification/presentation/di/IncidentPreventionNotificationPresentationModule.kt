package com.smartgas_frontend.incidentpreventionnotification.presentation.di

import com.smartgas_frontend.incidentpreventionnotification.data.di.IncidentPreventionNotificationDataModule
import com.smartgas_frontend.incidentpreventionnotification.presentation.alerts.AlertsViewModel
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IncidentPreventionNotificationPresentationModule {

    fun getAlertsViewModel(): AlertsViewModel {
        return AlertsViewModel(
            IncidentPreventionNotificationDataModule.getAlertRepository(),
            SharedDataModule.getSessionService()
        )
    }
}
