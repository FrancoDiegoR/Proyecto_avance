package com.smartgas_frontend.shared.domain.model

import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Alert
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription

data class HomeSummary(
    val sensors: List<Sensor> = emptyList(),
    val zones: List<Zone> = emptyList(),
    val activeIncidents: List<Incident> = emptyList(),
    val pendingAlerts: List<Alert> = emptyList(),
    val overallStatus: String = "Safe",
    val plan: Plan? = null,
    val subscription: Subscription? = null,
    val lastReading: SensorReading? = null,
    val criticalZone: Zone? = null
)

data class Weather(
    val temperature: Double?,
    val temperatureUnit: String,
    val relativeHumidity: Double?,
    val relativeHumidityUnit: String,
    val windSpeed: Double?,
    val windSpeedUnit: String,
    val source: String
)
