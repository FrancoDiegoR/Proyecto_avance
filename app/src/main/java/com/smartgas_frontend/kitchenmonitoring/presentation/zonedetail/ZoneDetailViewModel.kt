package com.smartgas_frontend.kitchenmonitoring.presentation.zonedetail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.kitchenmonitoring.data.repository.MonitoringRepository
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.kitchenmonitoring.presentation.components.withinLastHours
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.utils.dateMillis
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// Umbral por defecto del backend (Setting.GasThreshold = 50).
// El valor configurable vive en el bounded context iam (Settings); no se lee aqui
// para no acoplar kitchenmonitoring con iam.
const val DEFAULT_GAS_THRESHOLD = 50.0

data class ZoneDetailData(
    val zone: Zone? = null,
    val sensors: List<Sensor> = emptyList(),
    val readings: List<SensorReading> = emptyList(),
    val gasThreshold: Double = DEFAULT_GAS_THRESHOLD
) {
    /** Lecturas de las ultimas 6 horas, ordenadas por fecha (para el grafico). */
    val chartReadings: List<SensorReading> get() = readings.withinLastHours(6)

    /** Porcentaje del gas actual respecto al umbral (ej. 41 de 50 = 82 %). */
    val gasPercentOfThreshold: Int?
        get() {
            val gas = zone?.gasLevel ?: return null
            if (gasThreshold <= 0.0) return null
            return ((gas / gasThreshold) * 100).toInt()
        }

    /** Ultima lectura de un sensor (para mostrar su valor actual en la lista). */
    fun latestReadingFor(sensor: Sensor): SensorReading? =
        readings
            .filter { it.sensorId == sensor.id || it.sensorCode.equals(sensor.code, ignoreCase = true) }
            .maxByOrNull { dateMillis(it.timestamp) }
}

class ZoneDetailViewModel(
    private val repository: MonitoringRepository,
    private val sessionService: SessionService,
    private val zoneId: Int
) : ViewModel() {

    private val _state = mutableStateOf(UIState<ZoneDetailData>(isLoading = true))
    val state: State<UIState<ZoneDetailData>> get() = _state

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val zones = async { repository.getZones(accountId) }
            val sensors = async { repository.getSensors(accountId) }
            val readings = async { repository.getSensorReadings(accountId) }

            val zonesResult = zones.await()
            val sensorsResult = sensors.await()
            val readingsResult = readings.await()

            if (zonesResult is Resource.Success && sensorsResult is Resource.Success && readingsResult is Resource.Success) {
                val zone = zonesResult.data.orEmpty().find { it.id == zoneId }

                if (zone == null) {
                    _state.value = UIState(message = "Zone not found")
                    return@launch
                }

                _state.value = UIState(
                    data = ZoneDetailData(
                        zone = zone,
                        sensors = sensorsResult.data.orEmpty().filter { it.zoneId == zoneId },
                        readings = readingsResult.data.orEmpty().filter { it.zoneId == zoneId }
                    )
                )
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }
}
