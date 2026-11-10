package com.smartgas_frontend.incidentdetection.presentation.history

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.incidentdetection.data.repository.IncidentRepository
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.UiText
import com.smartgas_frontend.shared.utils.dateMillis
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.parseDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

// Chips rapidos del diseno 14 (se elige uno; tocarlo otra vez lo quita)
const val QUICK_CRITICAL = "critical"
const val QUICK_HIGH = "high"
const val QUICK_FALSE_ALARM = "falsealarm"

fun isActiveIncident(incident: Incident): Boolean {
    val status = normalizeStatus(incident.status)
    return status == "active" || status == "reviewed"
}

fun canReview(incident: Incident): Boolean = normalizeStatus(incident.status) == "active"

fun canClose(incident: Incident): Boolean = isActiveIncident(incident)

/** Filtros de la pantalla. Todos opcionales: sin filtros se ve todo el historial. */
data class HistoryFilters(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val zone: String? = null,
    val quick: String? = null
)

data class HistoryData(
    val incidents: List<Incident> = emptyList(),
    val filters: HistoryFilters = HistoryFilters()
) {
    /** Zonas que aparecen en el historial, para el desplegable "All zones". */
    val zones: List<String> get() = incidents.map { it.zoneName }.distinct().sorted()

    val filtered: List<Incident>
        get() = incidents
            .filter { incident ->
                val day = parseDate(incident.detectedAt)?.atZone(ZoneId.systemDefault())?.toLocalDate()
                val inRange = (filters.from == null || (day != null && !day.isBefore(filters.from))) &&
                    (filters.to == null || (day != null && !day.isAfter(filters.to)))
                val inZone = filters.zone == null || incident.zoneName == filters.zone
                val matchesQuick = when (filters.quick) {
                    QUICK_CRITICAL -> incident.severity.equals("critical", ignoreCase = true)
                    QUICK_HIGH -> incident.severity.lowercase() in listOf("high", "warning")
                    QUICK_FALSE_ALARM -> normalizeStatus(incident.status) == "falsealarm"
                    else -> true
                }
                inRange && inZone && matchesQuick
            }
            .sortedByDescending { dateMillis(it.detectedAt) }

    // Metricas del diseno 14 (sobre lo filtrado)
    val totalCount: Int get() = filtered.size
    val resolvedCount: Int get() = filtered.count { normalizeStatus(it.status) == "resolved" }

    /** Tiempo medio entre la deteccion y el cierre; null si aun no hay incidentes cerrados con fecha. */
    val averageResponseMillis: Long?
        get() {
            val durations = filtered.mapNotNull { incident ->
                val start = dateMillis(incident.detectedAt)
                val end = dateMillis(incident.resolvedAt)
                if (start > 0 && end > start) end - start else null
            }
            return if (durations.isEmpty()) null else durations.average().toLong()
        }
}

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

class HistoryViewModel(
    private val repository: IncidentRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<HistoryData>(isLoading = true, data = HistoryData()))
    val state: State<UIState<HistoryData>> get() = _state

    private fun current(): HistoryData = _state.value.data ?: HistoryData()

    private fun updateFilters(change: (HistoryFilters) -> HistoryFilters) {
        val data = current()
        _state.value = UIState(
            isLoading = _state.value.isLoading,
            data = data.copy(filters = change(data.filters)),
            message = _state.value.message
        )
    }

    fun onDateRangeChanged(from: LocalDate?, to: LocalDate?) = updateFilters { it.copy(from = from, to = to) }

    fun onZoneChanged(zone: String?) = updateFilters { it.copy(zone = zone) }

    /** Tocar el chip activo lo desactiva. */
    fun onQuickFilterToggled(quick: String) = updateFilters { it.copy(quick = if (it.quick == quick) null else quick) }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = current())

        viewModelScope.launch {
            val result = repository.getIncidents(sessionService.getAccountId())

            if (result is Resource.Success) {
                _state.value = UIState(data = current().copy(incidents = result.data.orEmpty()))
            } else {
                _state.value = UIState(data = current(), message = result.message ?: "An error occurred")
                toast(ToastSeverity.Error, text("Incidents could not be loaded.", "No se pudieron cargar los incidentes."))
            }
        }
    }
}
