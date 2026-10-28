package com.smartgas_frontend.postincidentprocedures.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.incidentdetection.data.remote.IncidentService
import com.smartgas_frontend.incidentpreventionnotification.data.remote.AlertService
import com.smartgas_frontend.incidentpreventionnotification.data.remote.dto.toAlert
import com.smartgas_frontend.incidentdetection.data.remote.dto.toIncident
import com.smartgas_frontend.kitchenmonitoring.data.remote.MonitoringService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toZone
import com.smartgas_frontend.postincidentprocedures.domain.model.SecurityReport
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class ReportRepository(
    private val incidentService: IncidentService,
    private val alertService: AlertService,
    private val monitoringService: MonitoringService
) {

    suspend fun getReportData(accountId: Int): Resource<SecurityReport> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val incidents = async { incidentService.getIncidents(accountId).bodyOrThrow() }
                val alerts = async { alertService.getAlerts(accountId).bodyOrThrow() }
                val zones = async { monitoringService.getZones(accountId).bodyOrThrow() }

                SecurityReport(
                    incidents = incidents.await().map { it.toIncident() },
                    alerts = alerts.await().map { it.toAlert() },
                    zones = zones.await().map { it.toZone() }
                )
            }
        }
    }
}
