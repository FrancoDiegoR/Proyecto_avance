package com.smartgas_frontend.kitchenmonitoring.presentation.addsensor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class AddSensorData(
    val zones: List<Zone> = emptyList(),
    val sensors: List<Sensor> = emptyList(),
    val currentPlan: Plan? = null
) {
    val maxSensors: Int get() = currentPlan?.maxSensors?.takeIf { it != 0 } ?: 5
    val unlimitedSensors: Boolean get() = maxSensors == UNLIMITED
    val canAddSensor: Boolean get() = unlimitedSensors || sensors.size < maxSensors

    /** Siguiente codigo libre (SG-001, SG-002...), igual que en la pantalla de sensores. */
    fun nextCode(): String {
        val max = sensors.maxOfOrNull { sensor -> sensor.code.filter { it.isDigit() }.toIntOrNull() ?: 0 } ?: 0
        return "SG-${(max + 1).toString().padStart(3, '0')}"
    }
}

/**
 * Formulario de sensor (diseno 08). Sirve para agregar y para editar:
 * - [editingSensorId] == null -> agregar. [initialZoneId] deja preseleccionada la zona de Zone detail.
 * - [editingSensorId] != null -> editar (lapiz del diseno 07): carga los datos del sensor en el formulario.
 */
class AddSensorViewModel(
    private val repository: SensorRepository,
    private val sessionService: SessionService,
    private val initialZoneId: Int?,
    private val editingSensorId: Int? = null
) : ViewModel() {

    val isEditing: Boolean get() = editingSensorId != null

    private val _state = mutableStateOf(UIState<AddSensorData>(isLoading = true))
    val state: State<UIState<AddSensorData>> get() = _state

    private val _form = mutableStateOf(SensorDraft())
    val form: State<SensorDraft> get() = _form

    // Id del texto de error (strings.xml) para traducirlo en la pantalla.
    private val _formError = mutableStateOf<Int?>(null)
    val formError: State<Int?> get() = _formError

    private val _saving = mutableStateOf(false)
    val saving: State<Boolean> get() = _saving

    private val data: AddSensorData get() = _state.value.data ?: AddSensorData()

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
                val loaded = AddSensorData(
                    zones = zonesResult.data.orEmpty(),
                    sensors = sensorsResult.data.orEmpty(),
                    currentPlan = planResult.data
                )
                _state.value = UIState(data = loaded)

                val editingSensor = loaded.sensors.find { it.id == editingSensorId }

                _form.value = if (editingSensor != null) {
                    // Editar: el formulario arranca con los datos actuales del sensor
                    SensorDraft(
                        name = editingSensor.name,
                        code = editingSensor.code,
                        type = editingSensor.type,
                        zoneId = editingSensor.zoneId,
                        locationDetail = editingSensor.locationDetail
                    )
                } else {
                    // Agregar: siguiente codigo libre y la zona de la que se vino
                    SensorDraft(
                        code = loaded.nextCode(),
                        zoneId = loaded.zones.find { it.id == initialZoneId }?.id ?: loaded.zones.firstOrNull()?.id
                    )
                }
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }

    fun onFormChanged(form: SensorDraft) {
        _form.value = form
    }

    /** Crea o actualiza el sensor; [onSaved] se llama solo si el backend respondio bien. */
    fun saveSensor(onSaved: () -> Unit) {
        val form = _form.value

        _formError.value = null

        if (data.zones.isEmpty()) {
            _formError.value = R.string.createZoneFirst
            return
        }

        if (form.name.isBlank() || form.code.isBlank() || form.type.isBlank() || form.zoneId == null) {
            _formError.value = R.string.emptyFields
            return
        }

        // El limite del plan solo aplica al agregar; editar no suma sensores
        if (!isEditing && !data.canAddSensor) {
            _formError.value = R.string.sensorLimitReached
            return
        }

        _saving.value = true

        viewModelScope.launch {
            val result = if (editingSensorId != null) {
                repository.updateSensor(editingSensorId, form)
            } else {
                repository.createSensor(form.copy(accountId = sessionService.getAccountId()))
            }

            if (result is Resource.Success) {
                onSaved()
            } else {
                _formError.value = when (result.message) {
                    CODE_EXISTS -> R.string.codeDuplicate
                    SENSOR_LIMIT_REACHED -> R.string.sensorLimitReached
                    else -> R.string.errorSaving
                }
            }

            _saving.value = false
        }
    }
}
