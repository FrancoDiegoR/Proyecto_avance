package com.smartgas_frontend.shared.data.repository

import com.smartgas_frontend.common.Constants
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.incidentdetection.data.remote.IncidentService
import com.smartgas_frontend.incidentpreventionnotification.data.remote.AlertService
import com.smartgas_frontend.incidentpreventionnotification.data.remote.dto.toAlert
import com.smartgas_frontend.incidentdetection.data.remote.dto.toIncident
import com.smartgas_frontend.kitchenmonitoring.data.remote.SensorService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensor
import com.smartgas_frontend.kitchenmonitoring.data.remote.MonitoringService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensorReading
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toZone
import com.smartgas_frontend.paymentmanagement.data.remote.SubscriptionService
import com.smartgas_frontend.paymentmanagement.data.remote.dto.matchPlan
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toPlan
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toSubscription
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.remote.ExternalWeatherService
import com.smartgas_frontend.shared.data.remote.dto.toWeather
import com.smartgas_frontend.shared.domain.model.HomeSummary
import com.smartgas_frontend.shared.domain.model.Weather
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import com.smartgas_frontend.shared.utils.dateMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class HomeRepository(
    private val sensorService: SensorService,
    private val monitoringService: MonitoringService,
    private val incidentService: IncidentService,
    private val alertService: AlertService,
    private val subscriptionService: SubscriptionService,
    private val weatherService: ExternalWeatherService
) {

    private fun normalizeStatus(status: String): String = if (status == "Active") "Warning" else status

    suspend fun getDashboardData(accountId: Int): Resource<HomeSummary> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val sensorsResponse = async { sensorService.getSensors(accountId).bodyOrThrow() }
                val zonesResponse = async { monitoringService.getZones(accountId).bodyOrThrow() }
                val incidentsResponse = async { incidentService.getIncidents(accountId).bodyOrThrow() }
                val alertsResponse = async { alertService.getAlerts(accountId).bodyOrThrow() }
                val readingsResponse = async { monitoringService.getSensorReadings(accountId).bodyOrThrow() }
                val subscriptionResponse = async { subscriptionService.getCurrentSubscription(accountId).bodyOrThrow() }
                val plansResponse = async { subscriptionService.getPlans().bodyOrThrow() }

                val sensors = sensorsResponse.await().map { it.toSensor() }
                val incidents = incidentsResponse.await().map { it.toIncident() }
                val alerts = alertsResponse.await().map { it.toAlert() }
                val sensorReadings = readingsResponse.await()
                    .map { it.toSensorReading() }
                    .sortedByDescending { dateMillis(it.timestamp) }

                val latestReadingByZoneId = sensorReadings
                    .filter { it.zoneId != null }
                    .groupBy { it.zoneId }
                    .mapValues { (_, readings) -> readings.first() }

                val zones = zonesResponse.await().map { dto ->
                    val zone = dto.toZone()
                    val latestReading = latestReadingByZoneId[zone.id]

                    zone.copy(
                        gasLevel = latestReading?.gasValue ?: zone.gasLevel,
                        temperature = latestReading?.temperatureValue ?: zone.temperature,
                        status = normalizeStatus(zone.status)
                    )
                }

                val subscriptionDto = subscriptionResponse.await()
                val plans = plansResponse.await().map { it.toPlan() }
                val subscription = subscriptionDto.toSubscription(plans)

                val activeIncidents = incidents.filter { it.status == "Active" || it.status == "Reviewed" }
                val pendingAlerts = alerts.filter { it.status == "Active" || it.status == "Pending" }

                val overallStatus = when {
                    activeIncidents.any { it.severity == "Critical" } -> "Critical"
                    activeIncidents.isNotEmpty() || pendingAlerts.isNotEmpty() -> "Warning"
                    else -> "Safe"
                }

                val plan = subscriptionDto.matchPlan(plans) ?: Plan(
                    id = subscription.planId,
                    name = subscription.planName,
                    price = subscription.price,
                    maxZones = subscription.maxZones,
                    maxSensors = subscription.maxSensors
                )

                HomeSummary(
                    sensors = sensors,
                    zones = zones,
                    activeIncidents = activeIncidents,
                    pendingAlerts = pendingAlerts,
                    overallStatus = overallStatus,
                    plan = plan,
                    subscription = subscription,
                    lastReading = sensorReadings.firstOrNull(),
                    criticalZone = zones.find { it.status == "Critical" } ?: zones.find { it.status == "Warning" }
                )
            }
        }
    }

    suspend fun getCurrentWeather(
        latitude: Double = Constants.DEFAULT_LATITUDE,
        longitude: Double = Constants.DEFAULT_LONGITUDE
    ): Resource<Weather> = withContext(Dispatchers.IO) {
        safeCall { weatherService.getCurrentWeather(latitude, longitude).bodyOrThrow().toWeather() }
    }
}
