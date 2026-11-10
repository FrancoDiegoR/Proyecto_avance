package com.smartgas_frontend.incidentpreventionnotification.presentation.alertdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.incidentpreventionnotification.presentation.components.alertSeverityColor
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatIncidentNotification
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.dialPhone
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Green
import com.smartgas_frontend.ui.theme.Red
import com.smartgas_frontend.ui.theme.TagSuccess

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(val es: Boolean) {
    val title = if (es) "Alerta" else "Alert"
    val back = if (es) "Volver" else "Back"
    val refresh = if (es) "Actualizar" else "Refresh"
    val gas = "Gas"
    val temperature = if (es) "Temperatura" else "Temperature"
    val limit = if (es) "Límite" else "Limit"
    val whatToDo = if (es) "Qué hacer ahora" else "What to do now"
    val confirm = if (es) "Confirmar que lo recibí" else "Confirm I received it"
    val confirmed = if (es) "Recepción confirmada" else "Receipt confirmed"
    val callContact = if (es) "Llamar a mi contacto" else "Call my contact"
    val noContact = if (es) "Agrega un contacto de emergencia en Ajustes." else "Add an emergency contact in Settings."
    val call116 = if (es) "Llamar al 116 (Bomberos)" else "Call 116 (Fire department)"
    val falseAlarm = if (es) "Marcar como falsa alarma" else "Mark as false alarm"
    val seeIncident = if (es) "Ver incidente" else "See incident"
    val notFound = if (es) "No se encontró la alerta." else "Alert not found."
    val noDialer = if (es) "No hay una app de teléfono disponible." else "No phone app available."

    /** Pasos del diseno 12 segun el tipo de incidente. */
    fun steps(type: String): List<String> = when (normalizeStatus(type)) {
        "hightemperature" -> if (es) listOf(
            "Apaga las hornillas y equipos de la zona.",
            "Si hay aceite en llamas, no uses agua: tápalo o usa un extintor.",
            "Aleja a las personas de la cocina.",
            "Si el fuego no se controla, sal y llama al 116."
        ) else listOf(
            "Turn off the burners and appliances in the zone.",
            "If oil is burning, don't use water: cover it or use an extinguisher.",
            "Keep people away from the kitchen.",
            "If the fire is not under control, get out and call 116."
        )
        else -> if (es) listOf(
            "No enciendas ni apagues luces ni aparatos eléctricos.",
            "Cierra la llave del gas.",
            "Abre puertas y ventanas para ventilar.",
            "Sal del lugar y llama a tu contacto o al 116."
        ) else listOf(
            "Don't switch lights or electrical devices on or off.",
            "Close the gas valve.",
            "Open doors and windows to ventilate.",
            "Leave the area and call your contact or 116."
        )
    }
}

/** Diseno 12: detalle de alerta. */
@Composable
fun AlertDetailScreen(
    viewModel: AlertDetailViewModel,
    onBack: () -> Unit,
    onOpenIncident: (Int) -> Unit
) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val context = LocalContext.current
    val state = viewModel.state.value
    val data = state.data ?: AlertDetailData()
    val notification = data.notification
    val incident = data.incident
    var dialError by remember { mutableStateOf(false) }

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

        if (state.isLoading && notification == null) {
            LoadingState()
        }

        if (state.message.isNotEmpty() && notification == null) {
            AlertBanner(pageText.notFound)
        }

        if (notification != null) {
            val severity = incident?.severity?.ifBlank { null } ?: "Critical"
            val type = incident?.type.orEmpty()

            // Tarjeta principal: hora, tipo, zona y sensor
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = alertSeverityColor(severity)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = Color.White)
                        Text(
                            formatDate(incident?.detectedAt ?: notification.createdAt),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            trSeverity(strings, severity).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        if (type.isNotEmpty()) trIncidentType(strings, type) else pageText.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (incident != null) {
                        Text(
                            "${trZone(strings, incident.zoneName)} · ${incident.sensorCode.ifEmpty { "Sensor ${incident.sensorId ?: "—"}" }}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            formatIncidentNotification(strings, notification).ifEmpty { notification.message },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Valores detectados frente al limite configurado en Ajustes
            if (incident != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ValueCard(
                        label = pageText.gas,
                        value = incident.gasReading,
                        unit = "ppm",
                        limitLabel = "${pageText.limit} ${formatNumber(data.gasThreshold)} ppm",
                        overLimit = (incident.gasReading ?: 0.0) >= data.gasThreshold,
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        label = pageText.temperature,
                        value = incident.temperatureReading,
                        unit = "°C",
                        limitLabel = "${pageText.limit} ${formatNumber(data.temperatureThreshold)} °C",
                        overLimit = (incident.temperatureReading ?: 0.0) >= data.temperatureThreshold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            PanelCard(title = pageText.whatToDo) {
                pageText.steps(type).forEachIndexed { index, step ->
                    StepLine(number = index + 1, text = step)
                }
            }

            // Acciones del diseno 12
            if (notification.confirmed) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TagSuccess)
                    Text(pageText.confirmed, color = TagSuccess, fontWeight = FontWeight.SemiBold)
                }
            } else {
                PrimaryButton(
                    label = pageText.confirm,
                    icon = Icons.Outlined.CheckCircle,
                    onClick = viewModel::confirmReceipt,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SecondaryButton(
                label = if (data.contactName.isNotBlank()) "${pageText.callContact} · ${data.contactName}" else pageText.callContact,
                icon = Icons.Outlined.Call,
                enabled = data.contactPhone.isNotBlank(),
                onClick = { dialError = !dialPhone(context, data.contactPhone) },
                modifier = Modifier.fillMaxWidth()
            )
            if (data.contactPhone.isBlank()) {
                Text(pageText.noContact, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            PrimaryButton(
                label = pageText.call116,
                icon = Icons.Outlined.LocalFireDepartment,
                containerColor = Red,
                onClick = { dialError = !dialPhone(context, FIRE_DEPARTMENT_PHONE) },
                modifier = Modifier.fillMaxWidth()
            )

            if (dialError) {
                AlertBanner(pageText.noDialer)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (data.isIncidentOpen) {
                    TextButton(onClick = viewModel::markFalseAlarm) {
                        Text(pageText.falseAlarm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (incident != null) {
                    Tag(value = pageText.closedLabel(incident), severity = TagSeverity.Secondary)
                }
                if (incident != null) {
                    TextButton(onClick = { onOpenIncident(incident.id) }) {
                        Text(pageText.seeIncident, color = Blue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Etiqueta cuando el incidente ya se cerro. */
private fun PageText.closedLabel(incident: Incident): String = when (normalizeStatus(incident.status)) {
    "falsealarm" -> if (es) "Falsa alarma" else "False alarm"
    else -> if (es) "Resuelto" else "Resolved"
}

/** Tarjeta "Gas 62 ppm · Limite 50 ppm"; en rojo si supera el limite. */
@Composable
private fun ValueCard(
    label: String,
    value: Double?,
    unit: String,
    limitLabel: String,
    overLimit: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        value == null -> MaterialTheme.colorScheme.onSurfaceVariant
        overLimit -> Red
        else -> Green
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (value == null) "—" else formatNumber(value),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (value != null) {
                    Text(unit, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Text(limitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Paso numerado de "Que hacer ahora". */
@Composable
private fun StepLine(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Blue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
