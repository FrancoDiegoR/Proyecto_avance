package com.smartgas_frontend.incidentdetection.presentation.di

import com.smartgas_frontend.incidentdetection.data.di.IncidentDetectionDataModule
import com.smartgas_frontend.incidentdetection.presentation.history.HistoryViewModel
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IncidentDetectionPresentationModule {

    fun getHistoryViewModel(): HistoryViewModel {
        return HistoryViewModel(IncidentDetectionDataModule.getIncidentRepository(), SharedDataModule.getSessionService())
    }
}
