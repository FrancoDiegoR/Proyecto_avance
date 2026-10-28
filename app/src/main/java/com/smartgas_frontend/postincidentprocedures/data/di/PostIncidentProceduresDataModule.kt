package com.smartgas_frontend.postincidentprocedures.data.di

import com.smartgas_frontend.incidentdetection.data.di.IncidentDetectionDataModule
import com.smartgas_frontend.incidentpreventionnotification.data.di.IncidentPreventionNotificationDataModule
import com.smartgas_frontend.kitchenmonitoring.data.di.KitchenMonitoringDataModule
import com.smartgas_frontend.postincidentprocedures.data.repository.ReportRepository

object PostIncidentProceduresDataModule {

    fun getReportRepository(): ReportRepository {
        return ReportRepository(
            IncidentDetectionDataModule.getIncidentService(),
            IncidentPreventionNotificationDataModule.getAlertService(),
            KitchenMonitoringDataModule.getMonitoringService()
        )
    }
}
