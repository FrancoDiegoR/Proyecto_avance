package com.smartgas_frontend.incidentdetection.data.di

import com.smartgas_frontend.incidentdetection.data.remote.IncidentService
import com.smartgas_frontend.incidentdetection.data.repository.IncidentRepository
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IncidentDetectionDataModule {

    fun getIncidentService(): IncidentService {
        return SharedDataModule.getRetrofit().create(IncidentService::class.java)
    }

    fun getIncidentRepository(): IncidentRepository {
        return IncidentRepository(getIncidentService())
    }
}
