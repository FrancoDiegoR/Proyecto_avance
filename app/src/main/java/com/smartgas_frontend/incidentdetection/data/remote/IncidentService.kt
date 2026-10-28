package com.smartgas_frontend.incidentdetection.data.remote

import com.smartgas_frontend.incidentdetection.data.remote.dto.IncidentDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface IncidentService {
    @GET("incidents")
    suspend fun getIncidents(@Query("accountId") accountId: Int): Response<List<IncidentDto>>

    @PATCH("incidents/{id}/review")
    suspend fun reviewIncident(@Path("id") incidentId: Int): Response<IncidentDto>

    @PATCH("incidents/{id}/resolve")
    suspend fun resolveIncident(@Path("id") incidentId: Int): Response<IncidentDto>

    @PATCH("incidents/{id}/false-alarm")
    suspend fun markFalseAlarm(@Path("id") incidentId: Int): Response<IncidentDto>
}
