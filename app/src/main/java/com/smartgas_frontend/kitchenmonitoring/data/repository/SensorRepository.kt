package com.smartgas_frontend.kitchenmonitoring.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.kitchenmonitoring.data.remote.SensorService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.CreateSensorRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.UpdateSensorRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.data.remote.MonitoringService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensorReading
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toZone
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.paymentmanagement.data.remote.SubscriptionService
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toSubscription
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.remote.DomainException
import com.smartgas_frontend.shared.data.remote.UNLIMITED
import com.smartgas_frontend.shared.data.remote.bodyOrNull
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import com.smartgas_frontend.shared.utils.dateMillis
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.nowISO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

const val EMPTY_SENSOR_CODE = "EMPTY_SENSOR_CODE"
const val EMPTY_SENSOR_NAME = "EMPTY_SENSOR_NAME"
const val EMPTY_SENSOR_TYPE = "EMPTY_SENSOR_TYPE"
const val EMPTY_ZONE_ID = "EMPTY_ZONE_ID"
const val CODE_EXISTS = "CODE_EXISTS"
const val SENSOR_LIMIT_REACHED = "SENSOR_LIMIT_REACHED"

data class SensorDraft(
    val accountId: Int = 0,
    val name: String = "",
    val code: String = "",
    val type: String = "Gas",
    val zoneId: Int? = null,
    val locationDetail: String = ""
)

class SensorRepository(
    private val service: SensorService,
    private val monitoringService: MonitoringService,
    private val subscriptionService: SubscriptionService
) {

    private fun formatLastReading(sensor: Sensor, reading: SensorReading?): String {
        if (reading == null) return "—"

        val sensorType = sensor.type.lowercase()
        val gas = reading.gasValue
        val temperature = reading.temperatureValue

        return when {
            sensorType.contains("temperature") ->
                if (temperature != null) "Temp: ${formatNumber(temperature)} °C" else "—"
            sensorType.contains("gas") ->
                if (gas != null) "Gas: ${formatNumber(gas)} ppm" else "—"
            gas != null && temperature != null ->
                "Gas: ${formatNumber(gas)} ppm / Temp: ${formatNumber(temperature)} °C"
            gas != null -> "Gas: ${formatNumber(gas)} ppm"
            temperature != null -> "Temp: ${formatNumber(temperature)} °C"
            else -> "—"
        }
    }

    private fun findLatestReadingForSensor(sensor: Sensor, readings: List<SensorReading>): SensorReading? =
        readings
            .filter { it.sensorId == sensor.id || it.sensorCode.equals(sensor.code, ignoreCase = true) }
            .maxByOrNull { dateMillis(it.timestamp) }

    private fun normalizeSensor(sensor: Sensor, readings: List<SensorReading> = emptyList()): Sensor {
        val latestReading = findLatestReadingForSensor(sensor, readings)

        return sensor.copy(
            lastReading = formatLastReading(sensor, latestReading),
            lastConnected = latestReading?.timestamp ?: sensor.lastConnected ?: nowISO()
        )
    }

    private suspend fun fetchSensors(accountId: Int): List<Sensor> = coroutineScope {
        val sensors = async { service.getSensors(accountId).bodyOrThrow() }
        val readings = async { monitoringService.getSensorReadings(accountId).bodyOrNull().orEmpty() }

        val normalizedReadings = readings.await().map { it.toSensorReading() }

        sensors.await().map { normalizeSensor(it.toSensor(), normalizedReadings) }
    }

    private suspend fun fetchPlanForAccount(accountId: Int): Plan {
        val subscription = subscriptionService.getCurrentSubscription(accountId).bodyOrThrow().toSubscription()

        return Plan(
            id = subscription.planId,
            name = subscription.planName,
            price = subscription.price,
            maxZones = subscription.maxZones,
            maxSensors = subscription.maxSensors,
            status = subscription.status
        )
    }

    suspend fun getSensors(accountId: Int): Resource<List<Sensor>> = withContext(Dispatchers.IO) {
        safeCall { fetchSensors(accountId) }
    }

    suspend fun getZones(accountId: Int): Resource<List<Zone>> = withContext(Dispatchers.IO) {
        safeCall { monitoringService.getZones(accountId).bodyOrThrow().map { it.toZone() } }
    }

    suspend fun getPlanForAccount(accountId: Int): Resource<Plan> = withContext(Dispatchers.IO) {
        safeCall { fetchPlanForAccount(accountId) }
    }

    suspend fun createSensor(data: SensorDraft): Resource<Sensor> = withContext(Dispatchers.IO) {
        safeCall {
            val code = data.code.trim().uppercase()
            val name = data.name.trim()

            if (code.isEmpty()) throw DomainException(EMPTY_SENSOR_CODE)
            if (name.isEmpty()) throw DomainException(EMPTY_SENSOR_NAME)
            if (data.type.isEmpty()) throw DomainException(EMPTY_SENSOR_TYPE)
            val zoneId = data.zoneId ?: throw DomainException(EMPTY_ZONE_ID)

            val currentSensors = fetchSensors(data.accountId)

            if (currentSensors.any { it.code.trim().uppercase() == code }) {
                throw DomainException(CODE_EXISTS)
            }

            val maxSensors = fetchPlanForAccount(data.accountId).maxSensors

            if (maxSensors != UNLIMITED && maxSensors > 0 && currentSensors.size >= maxSensors) {
                throw DomainException(SENSOR_LIMIT_REACHED)
            }

            val response = service.createSensor(
                CreateSensorRequest(
                    accountId = data.accountId,
                    zoneId = zoneId,
                    code = code,
                    name = name,
                    type = data.type
                )
            )

            normalizeSensor(response.bodyOrThrow().toSensor())
        }
    }

    suspend fun updateSensor(id: Int, data: SensorDraft): Resource<Sensor> = withContext(Dispatchers.IO) {
        safeCall {
            val response = service.updateSensor(
                id,
                UpdateSensorRequest(
                    name = data.name,
                    code = data.code,
                    type = data.type,
                    zoneId = data.zoneId,
                    locationDetail = data.locationDetail
                )
            )

            normalizeSensor(response.bodyOrThrow().toSensor())
        }
    }

    suspend fun deactivateSensor(id: Int): Resource<Sensor> = withContext(Dispatchers.IO) {
        safeCall {
            normalizeSensor(service.updateSensor(id, UpdateSensorRequest(status = "Offline")).bodyOrThrow().toSensor())
        }
    }

    suspend fun reactivateSensor(id: Int): Resource<Sensor> = withContext(Dispatchers.IO) {
        safeCall {
            normalizeSensor(service.updateSensor(id, UpdateSensorRequest(status = "Online")).bodyOrThrow().toSensor())
        }
    }
}
