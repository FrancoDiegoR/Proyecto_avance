package com.smartgas_frontend.kitchenmonitoring.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.kitchenmonitoring.data.remote.SensorService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.data.remote.MonitoringService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.CreateSensorReadingRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.CreateZoneRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toReadingResult
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensorReading
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toZone
import com.smartgas_frontend.kitchenmonitoring.domain.model.ReadingResult
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.paymentmanagement.data.remote.SubscriptionService
import com.smartgas_frontend.paymentmanagement.data.remote.dto.matchPlan
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toPlan
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toSubscription
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.remote.DomainException
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.errorMessage
import com.smartgas_frontend.shared.data.remote.safeCall
import com.smartgas_frontend.shared.utils.dateMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

const val SENSOR_NOT_FOUND = "SENSOR_NOT_FOUND"
const val EMPTY_ZONE_NAME = "EMPTY_ZONE_NAME"
const val ZONE_LIMIT_REACHED = "ZONE_LIMIT_REACHED"

data class ZoneDraft(
    val name: String = "",
    val description: String = "",
    val sensitivity: String = "Medium"
)

class MonitoringRepository(
    private val service: MonitoringService,
    private val sensorService: SensorService,
    private val subscriptionService: SubscriptionService
) {

    private fun normalizeZone(zone: Zone, readings: List<SensorReading>, sensors: List<Sensor>): Zone {
        val latestReading = readings
            .filter { it.zoneId == zone.id }
            .maxByOrNull { dateMillis(it.timestamp) }

        val riskLevel = when (zone.status.lowercase()) {
            "critical" -> "Critical"
            "warning" -> "High"
            else -> "Low"
        }

        return zone.copy(
            gasLevel = if (latestReading != null) latestReading.gasValue ?: 0.0 else zone.gasLevel ?: 0.0,
            temperature = if (latestReading != null) latestReading.temperatureValue ?: 0.0 else zone.temperature ?: 0.0,
            sensorCount = sensors.count { it.zoneId == zone.id },
            riskLevel = riskLevel,
            lastUpdated = latestReading?.timestamp ?: zone.updatedAt ?: zone.createdAt
        )
    }

    private suspend fun fetchSensors(accountId: Int): List<Sensor> =
        sensorService.getSensors(accountId).bodyOrThrow().map { it.toSensor() }

    private suspend fun fetchSensorReadings(accountId: Int): List<SensorReading> =
        service.getSensorReadings(accountId).bodyOrThrow().map { it.toSensorReading() }

    suspend fun getZones(accountId: Int): Resource<List<Zone>> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val zones = async { service.getZones(accountId).bodyOrThrow() }
                val sensors = async { fetchSensors(accountId) }
                val readings = async { fetchSensorReadings(accountId) }

                val loadedSensors = sensors.await()
                val loadedReadings = readings.await()

                zones.await().map { normalizeZone(it.toZone(), loadedReadings, loadedSensors) }
            }
        }
    }

    suspend fun getSensors(accountId: Int): Resource<List<Sensor>> = withContext(Dispatchers.IO) {
        safeCall { fetchSensors(accountId) }
    }

    suspend fun getSensorReadings(accountId: Int): Resource<List<SensorReading>> = withContext(Dispatchers.IO) {
        safeCall { fetchSensorReadings(accountId) }
    }

    suspend fun processReading(
        accountId: Int,
        sensorId: Int,
        gasValue: Double?,
        temperatureValue: Double?
    ): Resource<ReadingResult> = withContext(Dispatchers.IO) {
        safeCall {
            val sensor = fetchSensors(accountId).find { it.id == sensorId }
                ?: throw DomainException(SENSOR_NOT_FOUND)

            val response = service.createSensorReading(
                CreateSensorReadingRequest(
                    sensorCode = sensor.code,
                    gasLevel = gasValue ?: 0.0,
                    temperature = temperatureValue ?: 0.0
                )
            )

            response.bodyOrThrow().toReadingResult()
        }
    }

    suspend fun getPlanForAccount(accountId: Int): Resource<Plan> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val subscriptionResponse = async { subscriptionService.getCurrentSubscription(accountId).bodyOrThrow() }
                val plansResponse = async { subscriptionService.getPlans().bodyOrThrow() }

                val subscriptionDto = subscriptionResponse.await()
                val plans = plansResponse.await().map { it.toPlan() }
                val subscription = subscriptionDto.toSubscription(plans)

                subscriptionDto.matchPlan(plans) ?: Plan(
                    id = subscription.planId,
                    name = subscription.planName,
                    maxZones = subscription.maxZones,
                    maxSensors = subscription.maxSensors
                )
            }
        }
    }

    private suspend fun postZone(accountId: Int, zoneDraft: ZoneDraft): Zone {
        val name = zoneDraft.name.trim()

        if (name.isEmpty()) {
            throw DomainException(EMPTY_ZONE_NAME)
        }

        val response = service.createZone(
            CreateZoneRequest(
                accountId = accountId,
                name = name,
                description = zoneDraft.description,
                sensitivity = zoneDraft.sensitivity.ifBlank { "Medium" }
            )
        )

        if (!response.isSuccessful) {
            val message = response.errorMessage().lowercase()

            if (response.code() == 409 || message.contains("limit") || message.contains("plan")) {
                throw DomainException(ZONE_LIMIT_REACHED)
            }
        }

        return response.bodyOrThrow().toZone()
    }

    suspend fun createZone(accountId: Int, zoneDraft: ZoneDraft): Resource<Zone> = withContext(Dispatchers.IO) {
        safeCall { postZone(accountId, zoneDraft) }
    }

    suspend fun saveStarterZones(accountId: Int, zonesDraft: List<ZoneDraft>): Resource<List<Zone>> =
        withContext(Dispatchers.IO) {
            safeCall {
                zonesDraft
                    .filter { it.name.isNotBlank() }
                    .map { postZone(accountId, it) }
            }
        }
}
