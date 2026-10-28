package com.smartgas_frontend.incidentdetection.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.incidentdetection.data.remote.IncidentService
import com.smartgas_frontend.incidentdetection.data.remote.dto.toIncident
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IncidentRepository(private val service: IncidentService) {

    suspend fun getIncidents(accountId: Int): Resource<List<Incident>> = withContext(Dispatchers.IO) {
        safeCall { service.getIncidents(accountId).bodyOrThrow().map { it.toIncident() } }
    }

    suspend fun markReviewed(incidentId: Int): Resource<Incident> = withContext(Dispatchers.IO) {
        safeCall { service.reviewIncident(incidentId).bodyOrThrow().toIncident() }
    }

    suspend fun markResolved(incidentId: Int): Resource<Incident> = withContext(Dispatchers.IO) {
        safeCall { service.resolveIncident(incidentId).bodyOrThrow().toIncident() }
    }

    suspend fun markFalseAlarm(incidentId: Int): Resource<Incident> = withContext(Dispatchers.IO) {
        safeCall { service.markFalseAlarm(incidentId).bodyOrThrow().toIncident() }
    }

    suspend fun addNote(incidentId: Int, note: String): Resource<Incident> =
        Resource.Error("Incident notes are not available in the current backend.")
}
