package com.smartgas_frontend.incidentpreventionnotification.presentation.alerts

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.incidentpreventionnotification.data.repository.AlertRepository
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.UiText
import kotlinx.coroutines.launch

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

class AlertsViewModel(
    private val repository: AlertRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<List<Notification>>(isLoading = true))
    val state: State<UIState<List<Notification>>> get() = _state

    init {
        loadData()
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val result = repository.getNotifications(sessionService.getAccountId())

            if (result is Resource.Success) {
                _state.value = UIState(data = result.data.orEmpty())
            } else {
                _state.value = UIState(data = _state.value.data, message = result.message ?: "An error occurred")
                toast(
                    ToastSeverity.Error,
                    text("Alerts could not be loaded.", "No se pudieron cargar las alertas.")
                )
            }
        }
    }

    private fun <T> runAction(
        action: suspend () -> Resource<T>,
        ok: UiText,
        error: UiText
    ) {
        viewModelScope.launch {
            if (action() is Resource.Success) {
                loadData()
                toast(ToastSeverity.Success, ok)
            } else {
                toast(ToastSeverity.Error, error)
            }
        }
    }

    fun markNotificationRead(notification: Notification) = runAction(
        action = { repository.markNotificationRead(notification.id) },
        ok = text("Notification marked as read.", "Notificación marcada como leída."),
        error = text("The notification could not be marked as read.", "No se pudo marcar la notificación.")
    )

    fun confirmNotification(notification: Notification) = runAction(
        action = { repository.confirmNotification(notification.id) },
        ok = text("Receipt confirmed.", "Recepción confirmada."),
        error = text("Receipt could not be confirmed.", "No se pudo confirmar la recepción.")
    )
}
