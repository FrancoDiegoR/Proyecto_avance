package com.smartgas_frontend.incidentpreventionnotification.presentation.alerts

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

// Filtros del diseno 10: Active (n) · Confirmed · All
const val FILTER_ACTIVE = "active"
const val FILTER_CONFIRMED = "confirmed"
const val FILTER_ALL = "all"

/** Una alerta de la lista: la notificacion y, si existe, su incidente (para tipo, zona y valor). */
data class AlertItem(
    val notification: Notification,
    val incident: Incident?
)

data class AlertsData(
    val items: List<AlertItem> = emptyList(),
    val contactName: String = "",
    val contactPhone: String = "",
    val gasThreshold: Double = 50.0,
    val temperatureThreshold: Double = 45.0
) {
    /** "Activas" = aun no confirmadas por el usuario. */
    val activeItems: List<AlertItem> get() = items.filter { !it.notification.confirmed }
    val confirmedItems: List<AlertItem> get() = items.filter { it.notification.confirmed }

    fun filtered(filter: String): List<AlertItem> = when (filter) {
        FILTER_ACTIVE -> activeItems
        FILTER_CONFIRMED -> confirmedItems
        else -> items
    }
}

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

class AlertsViewModel(
    private val alertRepository: AlertRepository,
    private val incidentRepository: IncidentRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<AlertsData>(isLoading = true))
    val state: State<UIState<AlertsData>> get() = _state

    private val _filter = mutableStateOf(FILTER_ACTIVE)
    val filter: State<String> get() = _filter

    fun onFilterChanged(filter: String) {
        _filter.value = filter
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val notifications = async { alertRepository.getNotifications(accountId) }
            val incidents = async { incidentRepository.getIncidents(accountId) }
            val contact = async { settingsRepository.getEmergencyContact(accountId) }
            val settings = async { settingsRepository.getSettings(accountId) }

            val notificationsResult = notifications.await()

            if (notificationsResult !is Resource.Success) {
                _state.value = UIState(data = _state.value.data, message = notificationsResult.message ?: "An error occurred")
                toast(ToastSeverity.Error, text("Alerts could not be loaded.", "No se pudieron cargar las alertas."))
                return@launch
            }

            // Si fallan los incidentes, el contacto o los ajustes, la lista se muestra igual con lo que haya
            val incidentsById = incidents.await().data.orEmpty().associateBy { it.id }
            val contactData = contact.await().data
            val safety = settings.await().data?.safety

            _state.value = UIState(
                data = AlertsData(
                    items = notificationsResult.data.orEmpty().map { AlertItem(it, incidentsById[it.incidentId]) },
                    contactName = contactData?.name.orEmpty(),
                    contactPhone = contactData?.phone.orEmpty(),
                    gasThreshold = safety?.gasThreshold ?: 50.0,
                    temperatureThreshold = safety?.temperatureThreshold ?: 45.0
                )
            )
        }
    }

    /** Boton "Confirm" de cada tarjeta. */
    fun confirmNotification(notification: Notification) {
        viewModelScope.launch {
            if (alertRepository.confirmNotification(notification.id) is Resource.Success) {
                loadData()
                toast(ToastSeverity.Success, text("Receipt confirmed.", "Recepción confirmada."))
            } else {
                toast(ToastSeverity.Error, text("Receipt could not be confirmed.", "No se pudo confirmar la recepción."))
            }
        }
    }

    /** Icono ✓✓ de la cabecera: marca como leidas todas las alertas pendientes. */
    fun markAllAsRead() {
        val unread = _state.value.data?.items.orEmpty().map { it.notification }.filter { !it.read }

        if (unread.isEmpty()) {
            toast(ToastSeverity.Info, text("All alerts are already read.", "Todas las alertas ya están leídas."))
            return
        }

        viewModelScope.launch {
            val failed = unread.count { alertRepository.markNotificationRead(it.id) !is Resource.Success }

            loadData()

            if (failed == 0) {
                toast(ToastSeverity.Success, text("All alerts marked as read.", "Todas las alertas marcadas como leídas."))
            } else {
                toast(ToastSeverity.Error, text("Some alerts could not be updated.", "Algunas alertas no se pudieron actualizar."))
            }
        }
    }
}
