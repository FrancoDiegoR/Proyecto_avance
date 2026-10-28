package com.smartgas_frontend.shared.utils

import com.smartgas_frontend.R
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.data.remote.UNLIMITED
import com.smartgas_frontend.shared.i18n.Strings

private val zoneKeys = mapOf(
    "Main Kitchen" to R.string.zoneMainKitchen,
    "Cocina principal" to R.string.zoneMainKitchen,
    "Cocina Principal" to R.string.zoneMainKitchen,
    "Kitchen" to R.string.zoneMainKitchen,

    "Storage Room" to R.string.zoneWarehouse,
    "Warehouse" to R.string.zoneWarehouse,
    "Almacén" to R.string.zoneWarehouse,

    "Dining Area" to R.string.zoneDiningArea,
    "Comedor" to R.string.zoneDiningArea
)

private val zoneDescriptionKeys = mapOf(
    "Primary cooking area" to R.string.zoneMainKitchenDescription,
    "Storage and supply area" to R.string.zoneWarehouseDescription,
    "Customer dining space" to R.string.zoneDiningAreaDescription
)

private val statusKeys = mapOf(
    "Safe" to R.string.statusSafe,
    "Seguro" to R.string.statusSafe,

    "Warning" to R.string.statusWarning,
    "Advertencia" to R.string.statusWarning,

    "Critical" to R.string.statusCritical,
    "Crítico" to R.string.statusCritical,
    "Critico" to R.string.statusCritical,

    "Offline" to R.string.statusOffline,
    "Sin conexión" to R.string.statusOffline,

    "Online" to R.string.statusOnline,
    "En línea" to R.string.statusOnline,

    "Maintenance" to R.string.statusMaintenance,
    "Detected" to R.string.statusDetected,
    "Reviewed" to R.string.statusReviewed,
    "Resolved" to R.string.statusResolved,
    "False Alarm" to R.string.statusFalseAlarm,
    "FalseAlarm" to R.string.statusFalseAlarm,

    "Pending" to R.string.pending,
    "Active" to R.string.active,
    "Inactive" to R.string.inactive,
    "Approved" to R.string.statusApproved,
    "Cancelled" to R.string.statusCancelled
)

private val severityKeys = mapOf(
    "Low" to R.string.low,
    "Bajo" to R.string.low,

    "Medium" to R.string.medium,
    "Medio" to R.string.medium,

    "High" to R.string.high,
    "Alto" to R.string.high,

    "Warning" to R.string.warning,
    "Advertencia" to R.string.warning,

    "Critical" to R.string.critical,
    "Crítico" to R.string.critical,
    "Critico" to R.string.critical
)

private val typeKeys = mapOf(
    "GasLeak" to R.string.gasLeak,
    "Gas Leak" to R.string.gasLeak,
    "Fuga de Gas" to R.string.gasLeak,
    "Fuga de gas" to R.string.gasLeak,

    "HighTemperature" to R.string.highTemperature,
    "High Temperature" to R.string.highTemperature,
    "Temperatura Alta" to R.string.highTemperature,
    "Temperatura alta" to R.string.highTemperature,

    "GasLeakAndHighTemperature" to R.string.gasLeakAndHighTemperature,
    "Gas Leak And High Temperature" to R.string.gasLeakAndHighTemperature,
    "Gas Leak and High Temperature" to R.string.gasLeakAndHighTemperature,
    "Fuga de gas y temperatura alta" to R.string.gasLeakAndHighTemperature,

    "SmokeRisk" to R.string.smokeRisk,
    "Smoke Risk" to R.string.smokeRisk,

    "CORisk" to R.string.coRisk,
    "CO Risk" to R.string.coRisk
)

private val sensorTypeKeys = mapOf(
    "Gas" to R.string.sensorTypeGas,
    "Gas LP" to R.string.sensorTypeGas,
    "Temperature" to R.string.sensorTypeTemperature,
    "Temperatura" to R.string.sensorTypeTemperature,
    "CO" to R.string.sensorTypeCo,
    "Smoke" to R.string.sensorTypeSmoke,
    "Humo" to R.string.sensorTypeSmoke,
    "MultiSensor" to R.string.sensorTypeMulti,
    "Multi-sensor" to R.string.sensorTypeMulti,
    "Multisensor" to R.string.sensorTypeMulti
)

private val sensorNameKeys = mapOf(
    "Main Kitchen Sensor" to R.string.sensorMainKitchen,
    "Gas Sensor" to R.string.sensorGasGeneric,
    "Storage Gas Sensor" to R.string.sensorStorageGas,
    "Storage Temperature Sensor" to R.string.sensorStorageTemperature,
    "Backup-MultiSensor" to R.string.sensorBackupMulti,
    "Backup MultiSensor" to R.string.sensorBackupMulti,

    "Gas Sensor Alpha" to R.string.sensorGasAlpha,
    "Temp Sensor Alpha" to R.string.sensorTempAlpha,
    "Gas Sensor Beta" to R.string.sensorGasBeta,
    "Temp Sensor Beta" to R.string.sensorTempBeta,
    "Gas Sensor Gamma" to R.string.sensorGasGamma
)

private val locationKeys = mapOf(
    "Near stove" to R.string.locationNearStove,
    "Ceiling mount" to R.string.locationCeilingMount,
    "Near storage tanks" to R.string.locationNearStorageTanks,
    "Wall mount east" to R.string.locationWallMountEast,
    "Entrance wall" to R.string.locationEntranceWall
)

private val planNameKeys = mapOf(
    "Basic" to R.string.planBasicName,
    "Básico" to R.string.planBasicName,
    "Professional" to R.string.planProfessionalName,
    "Profesional" to R.string.planProfessionalName,
    "Corporate" to R.string.planCorporateName,
    "Corporativo" to R.string.planCorporateName
)

private val planDescriptionKeys = mapOf(
    "Basic" to R.string.planBasicDescription,
    "Professional" to R.string.planProfessionalDescription,
    "Corporate" to R.string.planCorporateDescription
)

private val planFeatureKeys = mapOf(
    "Basic monitoring" to R.string.planFeatureBasicMonitoring,
    "Web notifications" to R.string.planFeatureWebNotifications,
    "Incident history and response tracking" to R.string.planFeatureIncidentHistory,

    "Advanced monitoring" to R.string.planFeatureAdvancedMonitoring,
    "Reports" to R.string.planFeatureReports,
    "Priority alerts and escalation" to R.string.planFeaturePriorityAlerts,

    "Multi-zone monitoring" to R.string.planFeatureMultiZone,
    "Advanced reports" to R.string.planFeatureAdvancedReports,
    "Extended support" to R.string.planFeatureExtendedSupport,

    "Up to 5 sensors" to R.string.planBasicFeatureSensors,
    "Web alerts" to R.string.planBasicFeatureWebAlerts,
    "Basic incident reports" to R.string.planBasicFeatureReports,
    "Up to 3 zones" to R.string.planBasicFeatureZones,
    "Email support" to R.string.planBasicFeatureSupport,
    "Up to 12 sensors" to R.string.planProfessionalFeatureSensors,
    "SMS and email notifications" to R.string.planProfessionalFeatureChannels,
    "Incident history" to R.string.planProfessionalFeatureHistory,
    "Up to 5 zones" to R.string.planProfessionalFeatureZones,
    "Team access" to R.string.planProfessionalFeatureTeam,
    "Unlimited sensors" to R.string.planCorporateFeatureSensors,
    "Priority alerts" to R.string.planCorporateFeatureAlerts,
    "Advanced analytics" to R.string.planCorporateFeatureAnalytics,
    "Unlimited zones" to R.string.planCorporateFeatureZones,
    "Dedicated support" to R.string.planCorporateFeatureSupport,
    "Multiple locations" to R.string.planCorporateFeatureLocations
)

private val activityDetailKeys = mapOf(
    "Web browser · Lima, PE" to R.string.activityDetailWebLogin,
    "Chrome · Lima, PE" to R.string.activityDetailChromeLogin,
    "SG-005 · Dining Area" to R.string.activityDetailSensorGamma,
    "INC-001 · Warehouse" to R.string.activityDetailIncidentWarehouse,
    "Safety thresholds" to R.string.activityDetailSafetyThresholds,
    "Safety preferences" to R.string.activityDetailSafetyPreferences,
    "Password updated" to R.string.activityDetailPasswordUpdated,
    "SmartGas Basic" to R.string.activityDetailSmartGasBasic
)

fun translateValue(strings: Strings, value: String?, dictionary: Map<String, Int>): String {
    if (value.isNullOrEmpty()) return "—"

    val key = dictionary[value]

    return if (key != null) strings.t(key) else value
}

fun trZone(strings: Strings, name: String?) = translateValue(strings, name, zoneKeys)
fun trZoneDescription(strings: Strings, description: String?) = translateValue(strings, description, zoneDescriptionKeys)
fun trStatus(strings: Strings, status: String?) = translateValue(strings, status, statusKeys)
fun trSeverity(strings: Strings, severity: String?) = translateValue(strings, severity, severityKeys)
fun trIncidentType(strings: Strings, type: String?) = translateValue(strings, type, typeKeys)
fun trSensorType(strings: Strings, type: String?) = translateValue(strings, type, sensorTypeKeys)
fun trSensorName(strings: Strings, name: String?) = translateValue(strings, name, sensorNameKeys)
fun trLocationDetail(strings: Strings, detail: String?) = translateValue(strings, detail, locationKeys)
fun trPlanName(strings: Strings, plan: Plan?) = translateValue(strings, plan?.name, planNameKeys)
fun trPlanName(strings: Strings, name: String?) = translateValue(strings, name, planNameKeys)
fun trPlanDescription(strings: Strings, plan: Plan?) = translateValue(strings, plan?.name, planDescriptionKeys)
fun trPlanFeature(strings: Strings, feature: String?) = translateValue(strings, feature, planFeatureKeys)
fun trActivityDetail(strings: Strings, detail: String?) = translateValue(strings, detail, activityDetailKeys)

fun trAccountType(strings: Strings, type: String?): String = when (type) {
    "commercial" -> strings.t(R.string.commercialAccount)
    "domestic" -> strings.t(R.string.domesticAccount)
    else -> type?.takeIf { it.isNotEmpty() } ?: "—"
}

fun trLimit(strings: Strings, value: Int, noun: String = ""): String {
    if (value == UNLIMITED) {
        return if (noun == "zones") strings.t(R.string.unlimitedZonesShort) else strings.t(R.string.unlimitedShort)
    }

    return value.toString()
}

fun translatedMessageParams(strings: Strings, params: Map<String, String>): Array<Pair<String, Any?>> {
    val translated = params.toMutableMap()

    translated["severity"] = trSeverity(strings, params["severity"])
    translated["type"] = trIncidentType(strings, params["type"])
    translated["zone"] = trZone(strings, params["zone"])

    return translated.map { (key, value) -> key to value }.toTypedArray()
}

private val incidentMessageRegex =
    Regex("^Sensor (.+?) detected a (.+?) event in zone (.+?)\\.$", RegexOption.IGNORE_CASE)

fun formatIncidentNotification(strings: Strings, notification: Notification?): String {
    if (notification == null) return ""

    if (!notification.messageKey.isNullOrEmpty()) {
        return strings.t(notification.messageKey, *translatedMessageParams(strings, notification.messageParams))
    }

    val match = incidentMessageRegex.find(notification.message)

    if (match != null) {
        return strings.t(
            R.string.notificationIncidentDetected,
            "sensor" to match.groupValues[1],
            "type" to trIncidentType(strings, match.groupValues[2]),
            "zone" to trZone(strings, match.groupValues[3])
        )
    }

    return notification.message
}
