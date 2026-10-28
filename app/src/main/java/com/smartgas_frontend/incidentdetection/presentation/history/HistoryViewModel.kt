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
import kotlinx.coroutines.launch

const val TAB_ACTIVE = "active"
const val TAB_HISTORY = "history"

data class HistoryData(
    val incidents: List<Incident> = emptyList()
) {
    val activeIncidents: List<Incident>
        get() = incidents.filter(::isActiveIncident).sortedByDescending { dateMillis(it.detectedAt) }

    val historyIncidents: List<Incident>
        get() = incidents.filterNot(::isActiveIncident).sortedByDescending { dateMillis(it.detectedAt) }
}

fun isActiveIncident(incident: Incident): Boolean {
    val status = incident.status.lowercase()
    return status == "active" || status == "reviewed"
}

fun canReview(incident: Incident): Boolean = incident.status.lowercase() == "active"

fun canClose(incident: Incident): Boolean = isActiveIncident(incident)

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

class HistoryViewModel(
    private val repository: IncidentRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<HistoryData>(isLoading = true))
    val state: State<UIState<HistoryData>> get() = _state

    private val _activeTab = mutableStateOf(TAB_ACTIVE)
    val activeTab: State<String> get() = _activeTab

    init {
        loadData()
    }

    fun onTabChanged(tab: String) {
        _activeTab.value = tab
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val result = repository.getIncidents(sessionService.getAccountId())

            if (result is Resource.Success) {
                _state.value = UIState(data = HistoryData(incidents = result.data.orEmpty()))
            } else {
                _state.value = UIState(data = _state.value.data, message = result.message ?: "An error occurred")
                toast(
                    ToastSeverity.Error,
                    text("Incidents could not be loaded.", "No se pudieron cargar los incidentes.")
                )
            }
        }
    }

    private fun <T> runAction(
        action: suspend () -> Resource<T>,
        okSeverity: ToastSeverity,
        ok: UiText,
        error: UiText
    ) {
        viewModelScope.launch {
            if (action() is Resource.Success) {
                loadData()
                toast(okSeverity, ok)
            } else {
                toast(ToastSeverity.Error, error)
            }
        }
    }

    fun reviewIncident(incident: Incident) = runAction(
        action = { repository.markReviewed(incident.id) },
        okSeverity = ToastSeverity.Success,
        ok = text("Incident marked as reviewed.", "Incidente marcado como revisado."),
        error = text("The incident could not be reviewed.", "No se pudo revisar el incidente.")
    )

    fun resolveIncident(incident: Incident) = runAction(
        action = { repository.markResolved(incident.id) },
        okSeverity = ToastSeverity.Success,
        ok = text("Incident resolved.", "Incidente resuelto."),
        error = text("The incident could not be resolved.", "No se pudo resolver el incidente.")
    )

    fun markFalseAlarm(incident: Incident) = runAction(
        action = { repository.markFalseAlarm(incident.id) },
        okSeverity = ToastSeverity.Info,
        ok = text("Incident marked as false alarm.", "Incidente marcado como falsa alarma."),
        error = text("The incident could not be marked as false alarm.", "No se pudo marcar como falsa alarma.")
    )
}
