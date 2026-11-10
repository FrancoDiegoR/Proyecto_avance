package com.smartgas_frontend.incidentpreventionnotification.presentation.alertdetail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.iam.data.repository.SettingsRepository
import com.smartgas_frontend.incidentdetection.data.repository.IncidentRepository
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.incidentpreventionnotification.data.repository.AlertRepository
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/** Numero de los bomberos en Peru (boton "Llamar al 116" del diseno 12). */
const val FIRE_DEPARTMENT_PHONE = "116"

data class AlertDetailData(
    val notification: Notification? = null,
    val incident: Incident? = null,
    val contactName: String = "",
    val contactPhone: String = "",
    val gasThreshold: Double = 50.0,
    val temperatureThreshold: Double = 45.0
) {
    /** El incidente sigue abierto (Active o Reviewed): se puede marcar como falsa alarma. */
    val isIncidentOpen: Boolean
        get() = incident?.status?.lowercase() in listOf("active", "reviewed")
}

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

/**
 * Diseno 12: detalle de una alerta (notificacion) con su incidente.
 * Usa repositorios de otros contextos (incidentes de incidentdetection y contacto de iam),
 * igual que ProfileRepository; se los pasa el PresentationModule.
 */
class AlertDetailViewModel(
    private val alertRepository: AlertRepository,
    private val incidentRepository: IncidentRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionService: SessionService,
    private val notificationId: Int
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<AlertDetailData>(isLoading = true))
    val state: State<UIState<AlertDetailData>> get() = _state

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val notifications = async { alertRepository.getNotifications(accountId) }
            val incidents = async { incidentRepository.getIncidents(accountId) }
            val contact = async { settingsRepository.getEmergencyContact(accountId) }
            val settings = async { settingsRepository.getSettings(accountId) }

            val notificationsResult = notifications.await()
            val found = notificationsResult.data?.firstOrNull { it.id == notificationId }

            if (notificationsResult !is Resource.Success || found == null) {
                _state.value = UIState(data = _state.value.data, message = notificationsResult.message ?: "Not found")
                return@launch
            }

            // Abrir la alerta la marca como leida (sin toast: es automatico)
            val notification = if (found.read) {
                found
            } else {
                alertRepository.markNotificationRead(found.id).data ?: found.copy(read = true)
            }

            val incident = incidents.await().data?.firstOrNull { it.id == notification.incidentId }
            val contactData = contact.await().data
            val safety = settings.await().data?.safety

            _state.value = UIState(
                data = AlertDetailData(
                    notification = notification,
                    incident = incident,
                    contactName = contactData?.name.orEmpty(),
                    contactPhone = contactData?.phone.orEmpty(),
                    gasThreshold = safety?.gasThreshold ?: 50.0,
                    temperatureThreshold = safety?.temperatureThreshold ?: 45.0
                )
            )
        }
    }

    /** "Confirmar que lo recibi". */
    fun confirmReceipt() {
        val notification = _state.value.data?.notification ?: return

        viewModelScope.launch {
            if (alertRepository.confirmNotification(notification.id) is Resource.Success) {
                loadData()
                toast(ToastSeverity.Success, text("Receipt confirmed.", "Recepción confirmada."))
            } else {
                toast(ToastSeverity.Error, text("Receipt could not be confirmed.", "No se pudo confirmar la recepción."))
            }
        }
    }

    /** "Marcar como falsa alarma": cierra el incidente asociado. */
    fun markFalseAlarm() {
        val incident = _state.value.data?.incident ?: return

        viewModelScope.launch {
            if (incidentRepository.markFalseAlarm(incident.id) is Resource.Success) {
                loadData()
                toast(ToastSeverity.Info, text("Marked as false alarm.", "Marcada como falsa alarma."))
            } else {
                toast(
                    ToastSeverity.Error,
                    text("It could not be marked as false alarm.", "No se pudo marcar como falsa alarma.")
                )
            }
        }
    }
}
