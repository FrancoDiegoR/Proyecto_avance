package com.smartgas_frontend.incidentdetection.presentation.incidentdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DetailRow
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.TagSuccess
import com.smartgas_frontend.ui.theme.TagWarning

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(val es: Boolean) {
    val title = if (es) "Incidente" else "Incident"
    val back = if (es) "Volver" else "Back"
    val refresh = if (es) "Actualizar" else "Refresh"
    val zone = if (es) "Zona" else "Zone"
    val sensor = "Sensor"
    val detectedValue = if (es) "Valor detectado" else "Detected value"
    val timeline = if (es) "Línea de tiempo" else "Timeline"
    val review = if (es) "Marcar como revisado" else "Mark as reviewed"
    val resolve = if (es) "Marcar como resuelto" else "Mark as resolved"
    val falseAlarm = if (es) "Marcar como falsa alarma" else "Mark as false alarm"
    val notFound = if (es) "No se encontró el incidente." else "Incident not found."
    val pending = if (es) "Pendiente" else "Pending"

    fun stepLabel(key: String, incident: Incident?): String = when (key) {
        STEP_DETECTED -> if (es) "Detectado por el sensor" else "Detected by the sensor"
        STEP_NOTIFIED -> if (es) "Notificación enviada" else "Notification sent"
        STEP_CONFIRMED -> if (es) "Recepción confirmada" else "Receipt confirmed"
        STEP_REVIEWED -> if (es) "Revisado" else "Reviewed"
        else -> when (normalizeStatus(incident?.status)) {
            "falsealarm" -> if (es) "Cerrado como falsa alarma" else "Closed as false alarm"
            else -> if (es) "Resuelto" else "Resolved"
        }
    }
}

private fun severityStyle(severity: String?): TagSeverity = when (severity.orEmpty().lowercase()) {
    "critical" -> TagSeverity.Danger
    "high", "warning" -> TagSeverity.Warning
    "medium" -> TagSeverity.Info
    else -> TagSeverity.Success
}

private fun statusStyle(status: String?): TagSeverity = when (normalizeStatus(status)) {
    "active" -> TagSeverity.Danger
    "reviewed" -> TagSeverity.Warning
    "resolved" -> TagSeverity.Success
    "falsealarm" -> TagSeverity.Info
    else -> TagSeverity.Secondary
}

/** "Gas: 62 ppm · Temperatura: 30 °C" con lo que haya detectado el sensor. */
private fun detectedValueText(pageText: PageText, incident: Incident): String {
    val parts = listOfNotNull(
        incident.gasReading?.let { "Gas: ${formatNumber(it)} ppm" },
        incident.temperatureReading?.let { "${if (pageText.es) "Temperatura" else "Temperature"}: ${formatNumber(it)} °C" }
    )
    return parts.joinToString("\n").ifEmpty { incident.detectedValue.ifEmpty { "—" } }
}

/** Diseno 13: detalle de incidente. */
@Composable
fun IncidentDetailScreen(viewModel: IncidentDetailViewModel, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val state = viewModel.state.value
    val data = state.data ?: IncidentDetailData()
    val incident = data.incident

    ToastEffect(viewModel.toasts)

    // Se recarga cada vez que la pantalla vuelve a mostrarse.
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    ContentPage {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = pageText.back)
            }
            Text(
                text = pageText.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            IconButton(onClick = viewModel::loadData, enabled = !state.isLoading) {
                Icon(Icons.Outlined.Refresh, contentDescription = pageText.refresh)
            }
        }

        if (state.isLoading && incident == null) {
            LoadingState()
        }

        if (state.message.isNotEmpty() && incident == null) {
            AlertBanner(pageText.notFound)
        }

        if (incident != null) {
            PanelCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("#${incident.code}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            trIncidentType(strings, incident.type),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Tag(value = trSeverity(strings, incident.severity), severity = severityStyle(incident.severity))
                        Tag(value = trStatus(strings, incident.status), severity = statusStyle(incident.status))
                    }
                }
                Column {
                    DetailRow(pageText.zone, trZone(strings, incident.zoneName))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(pageText.sensor, incident.sensorCode.ifEmpty { "Sensor ${incident.sensorId ?: "—"}" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(pageText.detectedValue, detectedValueText(pageText, incident))
                }
            }

            PanelCard(title = pageText.timeline) {
                val steps = data.timeline
                Column {
                    steps.forEachIndexed { index, step ->
                        TimelineRow(
                            label = pageText.stepLabel(step.key, incident),
                            time = when {
                                !step.done -> pageText.pending
                                step.time != null -> formatDate(step.time)
                                else -> ""
                            },
                            done = step.done,
                            isLast = index == steps.lastIndex
                        )
                    }
                }
            }

            if (data.showReview) {
                SecondaryButton(label = pageText.review, onClick = viewModel::review, modifier = Modifier.fillMaxWidth())
            }
            if (data.showClose) {
                PrimaryButton(
                    label = pageText.resolve,
                    icon = Icons.Outlined.Check,
                    containerColor = TagSuccess,
                    onClick = viewModel::resolve,
                    modifier = Modifier.fillMaxWidth()
                )
                PrimaryButton(
                    label = pageText.falseAlarm,
                    containerColor = TagWarning,
                    onClick = viewModel::markFalseAlarm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Punto + linea vertical de la linea de tiempo. */
@Composable
private fun TimelineRow(label: String, time: String, done: Boolean, isLast: Boolean) {
    val dotColor = if (done) TagSuccess else MaterialTheme.colorScheme.outlineVariant

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(if (done) dotColor else Color.Transparent, CircleShape)
                    .border(2.dp, dotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Column(modifier = Modifier.padding(top = 1.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (time.isNotEmpty()) {
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
