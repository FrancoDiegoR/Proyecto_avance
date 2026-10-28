package com.smartgas_frontend.kitchenmonitoring.presentation.sensordetail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.kitchenmonitoring.data.repository.MonitoringRepository
import com.smartgas_frontend.kitchenmonitoring.data.repository.SensorRepository
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.kitchenmonitoring.presentation.components.withinLastHours
import com.smartgas_frontend.kitchenmonitoring.presentation.zonedetail.DEFAULT_GAS_THRESHOLD
import com.smartgas_frontend.shared.data.local.SessionService
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class SensorDetailData(
    val sensor: Sensor? = null,
    val zone: Zone? = null,
    val readings: List<SensorReading> = emptyList(),
    val gasThreshold: Double = DEFAULT_GAS_THRESHOLD
) {
    private val type: String get() = sensor?.type.orEmpty().lowercase()

    /** Que valores mide el sensor segun su tipo (Gas, Temperature, MultiSensor...). */
    val measuresGas: Boolean get() = !type.contains("temp")
    val measuresTemperature: Boolean get() = type.contains("temp") || type.contains("multi")

    /** Lecturas de las ultimas 24 horas, ordenadas por fecha (para el grafico). */
    val chartReadings: List<SensorReading> get() = readings.withinLastHours(24)

    val latestReading: SensorReading? get() = chartReadings.lastOrNull()
}

class SensorDetailViewModel(
    private val sensorRepository: SensorRepository,
    private val monitoringRepository: MonitoringRepository,
    private val sessionService: SessionService,
    private val sensorId: Int
) : ViewModel() {

    private val _state = mutableStateOf(UIState<SensorDetailData>(isLoading = true))
    val state: State<UIState<SensorDetailData>> get() = _state

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val sensors = async { sensorRepository.getSensors(accountId) }
            val zones = async { sensorRepository.getZones(accountId) }
            val readings = async { monitoringRepository.getSensorReadings(accountId) }

            val sensorsResult = sensors.await()
            val zonesResult = zones.await()
            val readingsResult = readings.await()

            if (sensorsResult is Resource.Success && zonesResult is Resource.Success && readingsResult is Resource.Success) {
                val sensor = sensorsResult.data.orEmpty().find { it.id == sensorId }

                if (sensor == null) {
                    _state.value = UIState(message = "Sensor not found")
                    return@launch
                }

                _state.value = UIState(
                    data = SensorDetailData(
                        sensor = sensor,
                        zone = zonesResult.data.orEmpty().find { it.id == sensor.zoneId },
                        readings = readingsResult.data.orEmpty().filter {
                            it.sensorId == sensor.id || it.sensorCode.equals(sensor.code, ignoreCase = true)
                        }
                    )
                )
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }
}
