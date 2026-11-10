package com.smartgas_frontend.incidentpreventionnotification.presentation.components

import androidx.compose.ui.graphics.Color
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Orange
import com.smartgas_frontend.ui.theme.Red
import com.smartgas_frontend.ui.theme.Yellow

// Estilos comunes de la lista de alertas (diseno 10) y del detalle (diseno 12).

/** Color segun la severidad: rojo critico, naranja alto/advertencia, amarillo medio, azul bajo. */
fun alertSeverityColor(severity: String?): Color = when (severity.orEmpty().lowercase()) {
    "critical" -> Red
    "high", "warning" -> Orange
    "medium" -> Yellow
    else -> Blue
}

/** Indica si el incidente es de temperatura (para mostrar °C y el boton "View" en vez de "Call contact"). */
fun isTemperatureIncident(incident: Incident?): Boolean =
    normalizeStatus(incident?.type).contains("temperature") && !normalizeStatus(incident?.type).contains("gas")

/** "78 ppm (limit 50)" o "47 °C (limit 45 °C)", segun el tipo de incidente. */
fun incidentValueWithLimit(
    incident: Incident,
    gasThreshold: Double,
    temperatureThreshold: Double,
    limitWord: String
): String? {
    val gas = incident.gasReading
    val temperature = incident.temperatureReading

    return when {
        isTemperatureIncident(incident) && temperature != null ->
            "${formatNumber(temperature)} °C ($limitWord ${formatNumber(temperatureThreshold)} °C)"
        gas != null -> "${formatNumber(gas)} ppm ($limitWord ${formatNumber(gasThreshold)})"
        temperature != null -> "${formatNumber(temperature)} °C ($limitWord ${formatNumber(temperatureThreshold)} °C)"
        else -> null
    }
}
