package com.smartgas_frontend.incidentpreventionnotification.presentation.di

import com.smartgas_frontend.iam.data.di.IamDataModule
import com.smartgas_frontend.incidentdetection.data.di.IncidentDetectionDataModule
import com.smartgas_frontend.incidentpreventionnotification.data.di.IncidentPreventionNotificationDataModule
import com.smartgas_frontend.incidentpreventionnotification.presentation.alertdetail.AlertDetailViewModel
import com.smartgas_frontend.incidentpreventionnotification.presentation.alerts.AlertsViewModel
import com.smartgas_frontend.SmartGasApplication
import com.smartgas_frontend.incidentpreventionnotification.presentation.push.AlertNotifier
import com.smartgas_frontend.incidentpreventionnotification.presentation.push.PushAlertChecker
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IncidentPreventionNotificationPresentationModule {

    fun getAlertsViewModel(): AlertsViewModel {
        return AlertsViewModel(
            IncidentPreventionNotificationDataModule.getAlertRepository(),
            IncidentDetectionDataModule.getIncidentRepository(),
            IamDataModule.getSettingsRepository(),
            SharedDataModule.getSessionService()
        )
    }

    fun getAlertDetailViewModel(notificationId: Int): AlertDetailViewModel {
        return AlertDetailViewModel(
            IncidentPreventionNotificationDataModule.getAlertRepository(),
            IncidentDetectionDataModule.getIncidentRepository(),
            IamDataModule.getSettingsRepository(),
            SharedDataModule.getSessionService(),
            notificationId
        )
    }

    fun getAlertNotifier(): AlertNotifier {
        return AlertNotifier(SmartGasApplication.instance.applicationContext)
    }

    fun getPushAlertChecker(): PushAlertChecker {
        return PushAlertChecker(
            SmartGasApplication.instance.applicationContext,
            IncidentPreventionNotificationDataModule.getAlertRepository(),
            IncidentDetectionDataModule.getIncidentRepository(),
            IamDataModule.getSettingsRepository(),
            SharedDataModule.getSessionService(),
            SharedDataModule.getAppPreferences(),
            getAlertNotifier()
        )
    }
}
