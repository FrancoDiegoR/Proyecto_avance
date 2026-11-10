package com.smartgas_frontend.incidentpreventionnotification.presentation.alerts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.incidentpreventionnotification.presentation.components.alertSeverityColor
import com.smartgas_frontend.incidentpreventionnotification.presentation.components.incidentValueWithLimit
import com.smartgas_frontend.incidentpreventionnotification.presentation.components.isTemperatureIncident
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.SoftPill
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.dialPhone
import com.smartgas_frontend.shared.utils.formatIncidentNotification
import com.smartgas_frontend.shared.utils.formatListDateTime
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.TagSuccess

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(es: Boolean) {
    val title = if (es) "Alertas" else "Alerts"
    val markAllRead = if (es) "Marcar todas como leídas" else "Mark all as read"
    val active = if (es) "Activas" else "Active"
    val confirmedFilter = if (es) "Confirmadas" else "Confirmed"
    val all = if (es) "Todas" else "All"
    val confirm = if (es) "Confirmar" else "Confirm"
    val confirmed = if (es) "Confirmada" else "Confirmed"
    val callContact = if (es) "Llamar contacto" else "Call contact"
    val view = if (es) "Ver" else "View"
    val limit = if (es) "límite" else "limit"
    val footer =
        if (es) "Las alertas también se envían como notificaciones push."
        else "Alerts are also sent as push notifications."
    val noActive = if (es) "No tienes alertas pendientes." else "You have no pending alerts."
    val noConfirmed = if (es) "Aún no confirmaste ninguna alerta." else "You haven't confirmed any alerts yet."
    val noAlerts = if (es) "No hay alertas." else "No alerts."
}

private val incidentMessageRegex =
    Regex("^Sensor (.+?) detected a (.+?) event in zone (.+?)\\.$", RegexOption.IGNORE_CASE)

/** Texto de la notificacion cuando no hay incidente asociado (mismo criterio que la web). */
private fun notificationMessage(strings: Strings, notification: Notification): String {
    val translated = formatIncidentNotification(strings, notification)

    if (translated.isNotEmpty() && translated != notification.message) {
        return translated
    }

    val match = incidentMessageRegex.find(notification.message)

    if (match != null) {
        val sensor = match.groupValues[1]
        val type = trIncidentType(strings, match.groupValues[2])
        val zone = trZone(strings, match.groupValues[3])

        return if (strings.isSpanish) {
            "El sensor $sensor detectó un evento de $type en la zona $zone."
        } else {
            "Sensor $sensor detected a $type event in zone $zone."
        }
    }

    return notification.message.ifEmpty { "—" }
}

/** Diseno 10: Alerts. */
@Composable
fun AlertsScreen(viewModel: AlertsViewModel, onOpenAlert: (Int) -> Unit) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val context = LocalContext.current
    val state = viewModel.state.value
    val data = state.data ?: AlertsData()
    val filter = viewModel.filter.value
    val items = data.filtered(filter)

    ToastEffect(viewModel.toasts)

    // Se recarga cada vez que la pantalla vuelve a mostrarse (por ejemplo, al regresar del detalle).
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    ContentPage {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                pageText.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            IconButton(onClick = viewModel::markAllAsRead) {
                Icon(Icons.Outlined.DoneAll, contentDescription = pageText.markAllRead)
            }
        }

        // Chips de filtro: Active (n) · Confirmed · All
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filter == FILTER_ACTIVE,
                onClick = { viewModel.onFilterChanged(FILTER_ACTIVE) },
                label = { Text("${pageText.active} (${data.activeItems.size})") },
                shape = RoundedCornerShape(50)
            )
            FilterChip(
                selected = filter == FILTER_CONFIRMED,
                onClick = { viewModel.onFilterChanged(FILTER_CONFIRMED) },
                label = { Text(pageText.confirmedFilter) },
                shape = RoundedCornerShape(50)
            )
            FilterChip(
                selected = filter == FILTER_ALL,
                onClick = { viewModel.onFilterChanged(FILTER_ALL) },
                label = { Text(pageText.all) },
                shape = RoundedCornerShape(50)
            )
        }

        if (state.isLoading && state.data == null) {
            LoadingState()
        }

        items.forEach { item ->
            AlertCard(
                strings = strings,
                pageText = pageText,
                item = item,
                data = data,
                onOpen = { onOpenAlert(item.notification.id) },
                onConfirm = { viewModel.confirmNotification(item.notification) },
                onCall = { dialPhone(context, data.contactPhone) }
            )
        }

        if (!state.isLoading && items.isEmpty()) {
            PanelCard {
                EmptyMessage(
                    when (filter) {
                        FILTER_ACTIVE -> pageText.noActive
                        FILTER_CONFIRMED -> pageText.noConfirmed
                        else -> pageText.noAlerts
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            pageText.footer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Tarjeta del diseno 10: borde izquierdo de color, severidad, hora, titulo, zona · valor y botones. */
@Composable
private fun AlertCard(
    strings: Strings,
    pageText: PageText,
    item: AlertItem,
    data: AlertsData,
    onOpen: () -> Unit,
    onConfirm: () -> Unit,
    onCall: () -> Unit
) {
    val notification = item.notification
    val incident = item.incident
    val severity = incident?.severity?.ifBlank { null } ?: "Critical"
    val color = alertSeverityColor(severity)

    val title = if (incident != null) trIncidentType(strings, incident.type) else notificationMessage(strings, notification)
    val subtitle = incident?.let {
        listOfNotNull(
            trZone(strings, it.zoneName),
            incidentValueWithLimit(it, data.gasThreshold, data.temperatureThreshold, pageText.limit)
        ).joinToString(" · ")
    }

    // En alertas de gas el segundo boton llama al contacto; en las demas abre el detalle
    val canCall = !isTemperatureIncident(incident) && data.contactPhone.isNotBlank()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftPill(trSeverity(strings, severity), color)
                    Spacer(Modifier.weight(1f))
                    Text(
                        formatListDateTime(notification.createdAt, strings.isSpanish),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    // Las no leidas van en negrita, como en una bandeja de entrada
                    fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (notification.confirmed) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = TagSuccess, modifier = Modifier.size(18.dp))
                            Text(pageText.confirmed, color = TagSuccess, style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        PillButton(
                            label = pageText.confirm,
                            icon = Icons.Outlined.Check,
                            onClick = onConfirm,
                            highlighted = true
                        )
                    }
                    if (canCall) {
                        PillButton(label = pageText.callContact, icon = Icons.Outlined.Call, onClick = onCall)
                    } else {
                        PillButton(label = pageText.view, icon = Icons.AutoMirrored.Outlined.OpenInNew, onClick = onOpen)
                    }
                }
            }
        }
    }
}

/** Boton pequeno con forma de pildora; [highlighted] es el azul de "Confirm". */
@Composable
private fun PillButton(label: String, icon: ImageVector, onClick: () -> Unit, highlighted: Boolean = false) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (highlighted) Blue else MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (highlighted) Blue.copy(alpha = 0.10f) else Color.Transparent,
            contentColor = if (highlighted) Blue else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label)
    }
}
