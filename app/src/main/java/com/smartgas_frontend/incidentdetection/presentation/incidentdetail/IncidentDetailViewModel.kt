package com.smartgas_frontend.incidentdetection.presentation.incidentdetail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.incidentdetection.data.repository.IncidentRepository
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.incidentdetection.presentation.history.canClose
import com.smartgas_frontend.incidentdetection.presentation.history.canReview
import com.smartgas_frontend.incidentpreventionnotification.data.repository.AlertRepository
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.UiText
import com.smartgas_frontend.shared.utils.dateMillis
import com.smartgas_frontend.shared.utils.normalizeStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/** Un paso de la linea de tiempo del diseno 13. */
data class TimelineStep(
    val key: String,
    val done: Boolean,
    val time: String?
)

const val STEP_DETECTED = "detected"
const val STEP_NOTIFIED = "notified"
const val STEP_CONFIRMED = "confirmed"
const val STEP_REVIEWED = "reviewed"
const val STEP_CLOSED = "closed"

data class IncidentDetailData(
    val incident: Incident? = null,
    val notification: Notification? = null
) {
    val showReview: Boolean get() = incident != null && canReview(incident)
    val showClose: Boolean get() = incident != null && canClose(incident)

    /**
     * Detectado -> Notificado -> Confirmado -> Revisado -> Cerrado.
     * El backend no envia la fecha de revision, por eso "Revisado" solo sale del estado.
     */
    val timeline: List<TimelineStep>
        get() {
            val status = normalizeStatus(incident?.status)
            val closed = status == "resolved" || status == "falsealarm"

            return listOf(
                TimelineStep(STEP_DETECTED, done = incident != null, time = incident?.detectedAt),
                TimelineStep(STEP_NOTIFIED, done = notification != null, time = notification?.createdAt),
                TimelineStep(
                    STEP_CONFIRMED,
                    done = notification?.confirmed == true,
                    time = notification?.confirmedAt
                ),
                TimelineStep(STEP_REVIEWED, done = status == "reviewed" || closed, time = null),
                TimelineStep(STEP_CLOSED, done = closed, time = incident?.resolvedAt)
            )
        }
}

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

/** Diseno 13: detalle de un incidente. Las notificaciones vienen del contexto de alertas. */
class IncidentDetailViewModel(
    private val incidentRepository: IncidentRepository,
    private val alertRepository: AlertRepository,
    private val sessionService: SessionService,
    private val incidentId: Int
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<IncidentDetailData>(isLoading = true))
    val state: State<UIState<IncidentDetailData>> get() = _state

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val incidents = async { incidentRepository.getIncidents(accountId) }
            val notifications = async { alertRepository.getNotifications(accountId) }

            val incidentsResult = incidents.await()
            val incident = incidentsResult.data?.firstOrNull { it.id == incidentId }

            if (incidentsResult !is Resource.Success || incident == null) {
                _state.value = UIState(data = _state.value.data, message = incidentsResult.message ?: "Not found")
                return@launch
            }

            // Si hay varias notificaciones del incidente, la primera que se envio
            val notification = notifications.await().data
                ?.filter { it.incidentId == incidentId }
                ?.minByOrNull { dateMillis(it.createdAt) }

            _state.value = UIState(data = IncidentDetailData(incident = incident, notification = notification))
        }
    }

    private fun <T> runAction(action: suspend () -> Resource<T>, okSeverity: ToastSeverity, ok: UiText, error: UiText) {
        viewModelScope.launch {
            if (action() is Resource.Success) {
                loadData()
                toast(okSeverity, ok)
            } else {
                toast(ToastSeverity.Error, error)
            }
        }
    }

    fun review() = runAction(
        action = { incidentRepository.markReviewed(incidentId) },
        okSeverity = ToastSeverity.Success,
        ok = text("Incident marked as reviewed.", "Incidente marcado como revisado."),
        error = text("The incident could not be reviewed.", "No se pudo revisar el incidente.")
    )

    fun resolve() = runAction(
        action = { incidentRepository.markResolved(incidentId) },
        okSeverity = ToastSeverity.Success,
        ok = text("Incident resolved.", "Incidente resuelto."),
        error = text("The incident could not be resolved.", "No se pudo resolver el incidente.")
    )

    fun markFalseAlarm() = runAction(
        action = { incidentRepository.markFalseAlarm(incidentId) },
        okSeverity = ToastSeverity.Info,
        ok = text("Incident marked as false alarm.", "Incidente marcado como falsa alarma."),
        error = text("The incident could not be marked as false alarm.", "No se pudo marcar como falsa alarma.")
    )
}
