package com.smartgas_frontend.postincidentprocedures.presentation.di

import com.smartgas_frontend.postincidentprocedures.data.di.PostIncidentProceduresDataModule
import com.smartgas_frontend.postincidentprocedures.presentation.reports.ReportsViewModel
import com.smartgas_frontend.shared.data.di.SharedDataModule

object PostIncidentProceduresPresentationModule {

    fun getReportsViewModel(): ReportsViewModel {
        return ReportsViewModel(PostIncidentProceduresDataModule.getReportRepository(), SharedDataModule.getSessionService())
    }
}
