package com.smartgas_frontend.shared.presentation.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.repository.HomeRepository
import com.smartgas_frontend.shared.domain.model.HomeSummary
import com.smartgas_frontend.shared.domain.model.Weather
import com.smartgas_frontend.shared.utils.normalizeStatus
import kotlinx.coroutines.launch

/** Cada cuanto se actualiza el Home solo mientras esta en pantalla. */
const val HOME_REFRESH_MILLIS = 15_000L

class HomeViewModel(
    private val repository: HomeRepository,
    private val sessionService: SessionService
) : ViewModel() {

    private val _state = mutableStateOf(UIState<HomeSummary>(isLoading = true))
    val state: State<UIState<HomeSummary>> get() = _state

    private val _weatherState = mutableStateOf(UIState<Weather>())
    val weatherState: State<UIState<Weather>> get() = _weatherState

    // Momento (millis) de la ultima carga correcta, para "updated 5 s ago"
    private val _updatedAt = mutableStateOf(0L)
    val updatedAt: State<Long> get() = _updatedAt

    /** Nombre para el saludo "Good morning, Heli" (el primer nombre del usuario). */
    val firstName: String
        get() {
            val user = sessionService.getCurrentUser()
            val name = user?.fullName?.ifBlank { null } ?: user?.name?.ifBlank { null } ?: ""
            return name.trim().substringBefore(' ')
        }

    private fun loadWeather() {
        viewModelScope.launch {
            val result = repository.getCurrentWeather()

            _weatherState.value = if (result is Resource.Success) {
                UIState(data = result.data)
            } else {
                UIState(data = _weatherState.value.data, message = result.message ?: "An error occurred")
            }
        }
    }

    /** [silent] = actualizacion automatica: no muestra el indicador de carga. */
    fun loadData(silent: Boolean = false) {
        if (!silent) {
            _state.value = UIState(isLoading = true, data = _state.value.data)
        }
        loadWeather()

        viewModelScope.launch {
            val result = repository.getDashboardData(sessionService.getAccountId())
            val summary = result.data

            if (result is Resource.Success && summary != null) {
                _state.value = UIState(data = summary)
                // Si los datos vienen de la copia offline no se cuenta como actualizacion
                if (summary.offlineSavedAt == null) {
                    _updatedAt.value = System.currentTimeMillis()
                }
            } else {
                _state.value = UIState(data = _state.value.data, message = result.message ?: "An error occurred")
            }
        }
    }
}

fun effectiveOverallStatus(data: HomeSummary): String {
    val zoneStatuses = data.zones.map { normalizeStatus(it.status) }

    return when {
        "critical" in zoneStatuses -> "Critical"
        "warning" in zoneStatuses -> "Warning"
        data.activeIncidents.isNotEmpty() || data.pendingAlerts.isNotEmpty() -> "Warning"
        else -> data.overallStatus.ifEmpty { "Safe" }
    }
}
