package com.smartgas_frontend.kitchenmonitoring.presentation.sensors

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.kitchenmonitoring.data.repository.CODE_EXISTS
import com.smartgas_frontend.kitchenmonitoring.data.repository.SENSOR_LIMIT_REACHED
import com.smartgas_frontend.kitchenmonitoring.data.repository.SensorDraft
import com.smartgas_frontend.kitchenmonitoring.data.repository.SensorRepository
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.remote.UNLIMITED
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.res
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

val sensorTypes = listOf("Gas", "Temperature", "MultiSensor", "CO", "Smoke")

data class DevicesData(
    val sensors: List<Sensor> = emptyList(),
    val zones: List<Zone> = emptyList(),
    val currentPlan: Plan? = null
) {
    val maxSensors: Int get() = currentPlan?.maxSensors?.takeIf { it != 0 } ?: 5
    val unlimitedSensors: Boolean get() = maxSensors == UNLIMITED
    val canAddSensor: Boolean get() = unlimitedSensors || sensors.size < maxSensors
    val sensorLimitExceeded: Boolean get() = !unlimitedSensors && sensors.size > maxSensors
    val sensorLimitReached: Boolean get() = !unlimitedSensors && sensors.size >= maxSensors
}

class SensorsViewModel(
    private val repository: SensorRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<DevicesData>(isLoading = true))
    val state: State<UIState<DevicesData>> get() = _state

    private val _showFormDialog = mutableStateOf(false)
    val showFormDialog: State<Boolean> get() = _showFormDialog

    private val _editingId = mutableStateOf<Int?>(null)
    val editingId: State<Int?> get() = _editingId

    private val _form = mutableStateOf(SensorDraft())
    val form: State<SensorDraft> get() = _form

    private val _formError = mutableStateOf<Int?>(null)
    val formError: State<Int?> get() = _formError

    private val _formLoading = mutableStateOf(false)
    val formLoading: State<Boolean> get() = _formLoading

    private val _selectedSensor = mutableStateOf<Sensor?>(null)
    val selectedSensor: State<Sensor?> get() = _selectedSensor

    private val data: DevicesData get() = _state.value.data ?: DevicesData()

    init {
        loadData()
    }

    private fun nextCode(): String {
        val max = data.sensors.maxOfOrNull { sensor ->
            sensor.code.filter { it.isDigit() }.toIntOrNull() ?: 0
        } ?: 0

        return "SG-${(max + 1).toString().padStart(3, '0')}"
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val sensors = async { repository.getSensors(accountId) }
            val zones = async { repository.getZones(accountId) }
            val plan = async { repository.getPlanForAccount(accountId) }

            val sensorsResult = sensors.await()
            val zonesResult = zones.await()
            val planResult = plan.await()

            if (sensorsResult is Resource.Success && zonesResult is Resource.Success && planResult is Resource.Success) {
                _state.value = UIState(
                    data = DevicesData(
                        sensors = sensorsResult.data.orEmpty(),
                        zones = zonesResult.data.orEmpty(),
                        currentPlan = planResult.data
                    )
                )
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }

    fun onFormChanged(form: SensorDraft) {
        _form.value = form
    }

    fun openAdd() {
        if (data.zones.isEmpty()) {
            toast(ToastSeverity.Warning, res(R.string.createZoneFirst))
            return
        }

        if (!data.canAddSensor) {
            toast(ToastSeverity.Warning, res(R.string.sensorLimitReached))
            return
        }

        _editingId.value = null
        _form.value = SensorDraft(code = nextCode(), zoneId = data.zones.firstOrNull()?.id)
        _formError.value = null
        _showFormDialog.value = true
    }

    fun openEdit(sensor: Sensor) {
        _editingId.value = sensor.id
        _form.value = SensorDraft(
            name = sensor.name,
            code = sensor.code,
            type = sensor.type,
            zoneId = sensor.zoneId,
            locationDetail = sensor.locationDetail
        )
        _formError.value = null
        _showFormDialog.value = true
    }

    fun closeForm() {
        _showFormDialog.value = false
    }

    fun openDetail(sensor: Sensor) {
        _selectedSensor.value = sensor
    }

    fun closeDetail() {
        _selectedSensor.value = null
    }

    fun saveSensor() {
        val form = _form.value
        val editingId = _editingId.value

        _formError.value = null

        if (form.name.isEmpty() || form.code.isEmpty() || form.type.isEmpty() || form.zoneId == null) {
            _formError.value = R.string.emptyFields
            return
        }

        if (editingId == null && !data.canAddSensor) {
            _formError.value = R.string.sensorLimitReached
            return
        }

        _formLoading.value = true

        viewModelScope.launch {
            val result = if (editingId != null) {
                repository.updateSensor(editingId, form)
            } else {
                repository.createSensor(form.copy(accountId = sessionService.getAccountId()))
            }

            if (result is Resource.Success) {
                _showFormDialog.value = false
                loadData()
                toast(ToastSeverity.Success, res(R.string.deviceSaved))
            } else {
                _formError.value = when (result.message) {
                    CODE_EXISTS -> R.string.codeDuplicate
                    SENSOR_LIMIT_REACHED -> R.string.sensorLimitReached
                    else -> R.string.errorSaving
                }
            }

            _formLoading.value = false
        }
    }

    fun deactivate(sensor: Sensor) {
        viewModelScope.launch {
            if (repository.deactivateSensor(sensor.id) is Resource.Success) {
                loadData()
                toast(ToastSeverity.Info, res(R.string.deviceDeactivated))
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }
        }
    }

    fun reactivate(sensor: Sensor) {
        viewModelScope.launch {
            if (repository.reactivateSensor(sensor.id) is Resource.Success) {
                loadData()
                toast(ToastSeverity.Success, res(R.string.deviceReactivated))
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }
        }
    }
}
