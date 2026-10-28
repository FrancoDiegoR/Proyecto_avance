package com.smartgas_frontend.postincidentprocedures.presentation.reports

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Alert
import com.smartgas_frontend.postincidentprocedures.data.repository.ReportRepository
import com.smartgas_frontend.postincidentprocedures.domain.model.GeneratedReport
import com.smartgas_frontend.postincidentprocedures.domain.model.SecurityReport
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.utils.dateMillis
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.trZone
import kotlinx.coroutines.launch

data class ReportFilters(
    val zone: String = "",
    val severity: String = "",
    val status: String = "",
    val type: String = "",
    val from: String = "",
    val to: String = ""
)

private val resolvedAlertStatuses = listOf("resolved", "closed")
private val closedAlertStatuses = listOf("resolved", "closed", "confirmed", "dismissed", "falsealarm", "inactive")
private val pendingIncidentStatuses = listOf("active", "reviewed", "detected", "open", "pending")

fun isPendingAlert(alert: Alert): Boolean {
    if (alert.resolvedAt != null || alert.isResolved) return false
    return normalizeStatus(alert.status) !in closedAlertStatuses
}

fun isResolvedAlert(alert: Alert): Boolean {
    if (alert.resolvedAt != null || alert.isResolved) return true
    return normalizeStatus(alert.status) in resolvedAlertStatuses
}

fun isPendingIncident(incident: Incident): Boolean = normalizeStatus(incident.status) in pendingIncidentStatuses

fun isResolvedIncident(incident: Incident): Boolean = normalizeStatus(incident.status) in resolvedAlertStatuses

class ReportsViewModel(
    private val repository: ReportRepository,
    private val sessionService: SessionService
) : ViewModel() {

    private val _state = mutableStateOf(UIState<SecurityReport>(isLoading = true))
    val state: State<UIState<SecurityReport>> get() = _state

    private val _filters = mutableStateOf(ReportFilters())
    val filters: State<ReportFilters> get() = _filters

    private val _reportSummary = mutableStateOf("")
    val reportSummary: State<String> get() = _reportSummary

    private val _generatedReport = mutableStateOf<GeneratedReport?>(null)
    val generatedReport: State<GeneratedReport?> get() = _generatedReport

    private val report: SecurityReport get() = _state.value.data ?: SecurityReport()

    val filteredIncidents: List<Incident>
        get() {
            val filters = _filters.value
            val fromMillis = if (filters.from.isNotEmpty()) dateMillis(filters.from) else null
            val toMillis = if (filters.to.isNotEmpty()) dateMillis(filters.to + "T23:59:59") else null

            return report.incidents.filter { incident ->
                val detectedAt = dateMillis(incident.detectedAt)

                (filters.zone.isEmpty() || incident.zoneName == filters.zone) &&
                    (filters.severity.isEmpty() || incident.severity == filters.severity) &&
                    (filters.status.isEmpty() || normalizeStatus(incident.status) == normalizeStatus(filters.status)) &&
                    (filters.type.isEmpty() || incident.type == filters.type) &&
                    (fromMillis == null || detectedAt >= fromMillis) &&
                    (toMillis == null || detectedAt <= toMillis)
            }
        }

    val countBySeverity: Map<String, Int> get() = filteredIncidents.groupingBy { it.severity }.eachCount()

    val countByType: Map<String, Int> get() = filteredIncidents.groupingBy { it.type }.eachCount()

    val mostAffectedZone: String
        get() = filteredIncidents.groupingBy { it.zoneName }.eachCount().maxByOrNull { it.value }?.key ?: "—"

    val pendingCount: Int get() = report.alerts.count(::isPendingAlert)

    val resolvedCount: Int get() = report.alerts.count(::isResolvedAlert)

    val incidentTypes: List<String> get() = report.incidents.map { it.type }.filter { it.isNotEmpty() }.distinct()

    init {
        loadData()
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val result = repository.getReportData(sessionService.getAccountId())

            if (result is Resource.Success) {
                _state.value = UIState(data = result.data)
            } else {
                _state.value = UIState(data = _state.value.data, message = result.message ?: "An error occurred")
            }
        }
    }

    fun onFiltersChanged(filters: ReportFilters) {
        _filters.value = filters
    }

    fun applyFilters(strings: Strings) {
        _reportSummary.value =
            "${strings.t(R.string.filtersApplied)}: ${filteredIncidents.size} ${strings.t(R.string.incidentsFound)}."
    }

    fun clearFilters() {
        _filters.value = ReportFilters()
        _reportSummary.value = ""
        _generatedReport.value = null
    }

    fun generateReport(strings: Strings) {
        val filters = _filters.value
        val incidents = filteredIncidents

        val zoneLabel = if (filters.zone.isNotEmpty()) trZone(strings, filters.zone) else strings.t(R.string.allZonesOption)
        val periodLabel = if (filters.from.isNotEmpty() || filters.to.isNotEmpty()) {
            "${filters.from.ifEmpty { "—" }} - ${filters.to.ifEmpty { "—" }}"
        } else {
            strings.t(R.string.allPeriods)
        }

        _generatedReport.value = GeneratedReport(
            zoneLabel = zoneLabel,
            periodLabel = periodLabel,
            total = incidents.size,
            critical = incidents.count { it.severity == "Critical" },
            warning = incidents.count { it.severity == "Warning" || it.severity == "High" },
            resolved = incidents.count(::isResolvedIncident),
            pending = incidents.count(::isPendingIncident),
            mostAffectedZone = trZone(strings, mostAffectedZone)
        )

        _reportSummary.value = strings.t(R.string.reportSummaryText, "count" to incidents.size, "zone" to zoneLabel)
    }
}
