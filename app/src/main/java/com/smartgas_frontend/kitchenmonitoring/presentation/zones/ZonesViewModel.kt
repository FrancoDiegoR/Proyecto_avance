package com.smartgas_frontend.kitchenmonitoring.presentation.zones

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.data.repository.MonitoringRepository
import com.smartgas_frontend.kitchenmonitoring.data.repository.ZONE_LIMIT_REACHED
import com.smartgas_frontend.kitchenmonitoring.data.repository.ZoneDraft
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.remote.UNLIMITED
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.res
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trSeverity
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MonitoringData(
    val zones: List<Zone> = emptyList(),
    val sensors: List<Sensor> = emptyList(),
    val currentPlan: Plan? = null
) {
    val hasZones: Boolean get() = zones.isNotEmpty()
    val hasSensors: Boolean get() = sensors.isNotEmpty()

    val maxZones: Int get() = currentPlan?.maxZones?.takeIf { it != 0 } ?: 3
    val unlimitedZones: Boolean get() = maxZones == UNLIMITED
    val canAddZone: Boolean get() = unlimitedZones || zones.size < maxZones
    val zoneLimitExceeded: Boolean get() = !unlimitedZones && zones.size > maxZones
    val zoneLimitReached: Boolean get() = !unlimitedZones && zones.size >= maxZones
    val remainingZones: Int get() = (maxZones - zones.size).coerceAtLeast(0)

    fun sensorsForZone(zoneId: Int): Int = sensors.count { it.zoneId == zoneId && it.status != "Offline" }
}

class ZonesViewModel(
    private val repository: MonitoringRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<MonitoringData>(isLoading = true))
    val state: State<UIState<MonitoringData>> get() = _state

    private val _activeFilter = mutableStateOf("All")
    val activeFilter: State<String> get() = _activeFilter

    private val _zoneDraft = mutableStateOf(ZoneDraft())
    val zoneDraft: State<ZoneDraft> get() = _zoneDraft

    private val _showZoneDialog = mutableStateOf(false)
    val showZoneDialog: State<Boolean> get() = _showZoneDialog

    private val _zoneSaveLoading = mutableStateOf(false)
    val zoneSaveLoading: State<Boolean> get() = _zoneSaveLoading

    private val _showSimulateDialog = mutableStateOf(false)
    val showSimulateDialog: State<Boolean> get() = _showSimulateDialog

    private val _simSensorId = mutableStateOf<Int?>(null)
    val simSensorId: State<Int?> get() = _simSensorId

    private val _simGas = mutableStateOf("20")
    val simGas: State<String> get() = _simGas

    private val _simTemp = mutableStateOf("28")
    val simTemp: State<String> get() = _simTemp

    private val _simLoading = mutableStateOf(false)
    val simLoading: State<Boolean> get() = _simLoading

    private val data: MonitoringData get() = _state.value.data ?: MonitoringData()

    private val selectedSensorType: String
        get() = data.sensors.find { it.id == _simSensorId.value }?.type.orEmpty().lowercase()

    val allowsGasValue: Boolean
        get() = selectedSensorType == "gas" || selectedSensorType == "multisensor"

    val allowsTemperatureValue: Boolean
        get() = selectedSensorType == "temperature" || selectedSensorType == "multisensor"

    val activeSensors: List<Sensor> get() = data.sensors.filter { it.status != "Offline" }

    val zoneForSensor: Zone?
        get() {
            val sensor = data.sensors.find { it.id == _simSensorId.value } ?: return null
            return data.zones.find { it.id == sensor.zoneId }
        }

    val filteredZones: List<Zone>
        get() = if (_activeFilter.value == "All") data.zones else data.zones.filter { it.status == _activeFilter.value }

    init {
        loadData()
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val zones = async { repository.getZones(accountId) }
            val sensors = async { repository.getSensors(accountId) }
            val plan = async { repository.getPlanForAccount(accountId) }

            val zonesResult = zones.await()
            val sensorsResult = sensors.await()
            val planResult = plan.await()

            if (zonesResult is Resource.Success && sensorsResult is Resource.Success && planResult is Resource.Success) {
                _state.value = UIState(
                    data = MonitoringData(
                        zones = zonesResult.data.orEmpty(),
                        sensors = sensorsResult.data.orEmpty(),
                        currentPlan = planResult.data
                    )
                )
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }

    fun onFilterChanged(filter: String) {
        _activeFilter.value = filter
    }

    fun onZoneDraftChanged(draft: ZoneDraft) {
        _zoneDraft.value = draft
    }

    private fun resetZoneDraft() {
        _zoneDraft.value = ZoneDraft()
    }

    fun openZoneDialog() {
        resetZoneDraft()
        _showZoneDialog.value = true
    }

    fun closeZoneDialog() {
        _showZoneDialog.value = false
    }

    fun saveZone() {
        if (_zoneDraft.value.name.isBlank()) {
            toast(ToastSeverity.Warning, res(R.string.emptyFields))
            return
        }

        if (!data.canAddZone) {
            toast(ToastSeverity.Warning, res(R.string.zoneLimitReached))
            return
        }

        _zoneSaveLoading.value = true

        viewModelScope.launch {
            val result = repository.createZone(sessionService.getAccountId(), _zoneDraft.value)

            if (result is Resource.Success) {
                resetZoneDraft()
                loadData()
                _showZoneDialog.value = false
                toast(ToastSeverity.Success, res(R.string.zoneSaved))
            } else {
                toast(
                    ToastSeverity.Error,
                    res(if (result.message == ZONE_LIMIT_REACHED) R.string.zoneLimitReached else R.string.errorSaving)
                )
            }

            _zoneSaveLoading.value = false
        }
    }

    fun onSimSensorSelected(sensorId: Int?) {
        _simSensorId.value = sensorId

        if (!allowsGasValue) {
            _simGas.value = ""
        } else if (_simGas.value.isEmpty()) {
            _simGas.value = "20"
        }

        if (!allowsTemperatureValue) {
            _simTemp.value = ""
        } else if (_simTemp.value.isEmpty()) {
            _simTemp.value = "28"
        }
    }

    fun onSimGasChanged(value: String) {
        _simGas.value = value
    }

    fun onSimTempChanged(value: String) {
        _simTemp.value = value
    }

    fun randomize() {
        if (allowsGasValue) {
            _simGas.value = (Random.nextInt(70) + 5).toString()
        }

        if (allowsTemperatureValue) {
            _simTemp.value = (Random.nextInt(50) + 20).toString()
        }
    }

    fun openSimulate() {
        val firstSensor = activeSensors.firstOrNull() ?: return

        _simGas.value = "20"
        _simTemp.value = "28"
        onSimSensorSelected(firstSensor.id)
        _showSimulateDialog.value = true
    }

    fun closeSimulate() {
        _showSimulateDialog.value = false
    }

    fun processReading() {
        val sensorId = _simSensorId.value ?: return

        _simLoading.value = true

        viewModelScope.launch {
            val gasValue = if (allowsGasValue) _simGas.value.toDoubleOrNull() else null
            val temperatureValue = if (allowsTemperatureValue) _simTemp.value.toDoubleOrNull() else null

            val result = repository.processReading(sessionService.getAccountId(), sensorId, gasValue, temperatureValue)
            val reading = result.data

            if (result is Resource.Success && reading != null) {
                _showSimulateDialog.value = false
                loadData()

                toast(ToastSeverity.Success, res(R.string.readingSaved))

                if (reading.incidentCreated) {
                    toast(
                        severity = if (reading.severity == "Critical") ToastSeverity.Error else ToastSeverity.Warning,
                        summary = res(R.string.incidentCreated),
                        detail = { "${trIncidentType(it, reading.incidentType)} – ${trSeverity(it, reading.severity)}" }
                    )
                } else {
                    toast(ToastSeverity.Info, res(R.string.noIncidentCreated))
                }
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }

            _simLoading.value = false
        }
    }
}
