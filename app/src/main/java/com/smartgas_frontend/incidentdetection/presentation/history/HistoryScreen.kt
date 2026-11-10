package com.smartgas_frontend.incidentdetection.presentation.history

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.SoftPill
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatDuration
import com.smartgas_frontend.shared.utils.formatListDateTime
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Orange
import com.smartgas_frontend.ui.theme.Red
import com.smartgas_frontend.ui.theme.TagSecondary
import com.smartgas_frontend.ui.theme.TagSuccess
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(val es: Boolean) {
    val title = if (es) "Historial" else "History"
    val export = if (es) "Exportar" else "Export"
    val dateRange = if (es) "Rango de fechas" else "Date range"
    val allDates = if (es) "Todas las fechas" else "All dates"
    val pickRange = if (es) "Elegir fechas" else "Pick dates"
    val clear = if (es) "Quitar" else "Clear"
    val apply = if (es) "Aplicar" else "Apply"
    val cancel = if (es) "Cancelar" else "Cancel"
    val allZones = if (es) "Todas las zonas" else "All zones"
    val critical = if (es) "Crítico" else "Critical"
    val high = if (es) "Alto" else "High"
    val falseAlarm = if (es) "Falsa alarma" else "False alarm"
    val incidents = if (es) "Incidentes" else "Incidents"
    val resolved = if (es) "Resueltos" else "Resolved"
    val avgResponse = if (es) "Resp. promedio" else "Avg. response"
    val empty = if (es) "No hay incidentes con estos filtros." else "No incidents match these filters."
    val shareTitle = if (es) "Historial de incidentes SmartGas" else "SmartGas incident history"
}

/** Color de la etiqueta de estado (ACTIVE rojo, REVIEWED naranja, RESOLVED verde...). */
private fun statusColor(status: String?): Color = when (normalizeStatus(status)) {
    "active" -> Red
    "reviewed" -> Orange
    "resolved" -> TagSuccess
    "falsealarm" -> TagSecondary
    else -> Blue
}

private fun statusLabel(strings: Strings, pageText: PageText, status: String): String =
    if (normalizeStatus(status) == "falsealarm") pageText.falseAlarm else trStatus(strings, status)

/** "Sep 01 – Oct 05, 2026" */
private fun formatRange(from: LocalDate, to: LocalDate, spanish: Boolean): String {
    val locale = if (spanish) Locale("es") else Locale.US
    val short = DateTimeFormatter.ofPattern("MMM dd", locale)
    val long = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)
    return "${short.format(from)} – ${long.format(to)}"
}

/** Icono de descarga: comparte el historial filtrado como CSV (correo, Drive, WhatsApp...). */
private fun shareCsv(context: Context, pageText: PageText, incidents: List<Incident>) {
    val header = "code,type,zone,sensor,severity,status,detected_at,resolved_at"
    val rows = incidents.joinToString("\n") { incident ->
        listOf(
            incident.code, incident.type, incident.zoneName, incident.sensorCode, incident.severity,
            incident.status, formatDate(incident.detectedAt), formatDate(incident.resolvedAt)
        ).joinToString(",") { "\"" + it.replace("\"", "'") + "\"" }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, pageText.shareTitle)
        putExtra(Intent.EXTRA_TEXT, "$header\n$rows")
    }
    context.startActivity(Intent.createChooser(intent, pageText.export))
}

/** Diseno 14: History. */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onOpenIncident: (Int) -> Unit) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val context = LocalContext.current
    val state = viewModel.state.value
    val data = state.data ?: HistoryData()
    val filters = data.filters
    val incidents = data.filtered

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
            IconButton(onClick = { shareCsv(context, pageText, incidents) }, enabled = incidents.isNotEmpty()) {
                Icon(Icons.Outlined.FileDownload, contentDescription = pageText.export)
            }
        }

        DateRangeField(
            pageText = pageText,
            from = filters.from,
            to = filters.to,
            onChanged = viewModel::onDateRangeChanged
        )

        // Chips: All zones ▾ · Critical · High · False alarm
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            ZoneChip(
                pageText = pageText,
                strings = strings,
                zones = data.zones,
                selected = filters.zone,
                onSelected = viewModel::onZoneChanged
            )
            listOf(
                QUICK_CRITICAL to pageText.critical,
                QUICK_HIGH to pageText.high,
                QUICK_FALSE_ALARM to pageText.falseAlarm
            ).forEach { (key, label) ->
                FilterChip(
                    selected = filters.quick == key,
                    onClick = { viewModel.onQuickFilterToggled(key) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(50)
                )
            }
        }

        // Metricas: Incidents · Resolved · Avg. response
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(pageText.incidents, data.totalCount.toString(), Modifier.weight(1f))
            StatCard(pageText.resolved, data.resolvedCount.toString(), Modifier.weight(1f))
            StatCard(pageText.avgResponse, data.averageResponseMillis?.let(::formatDuration) ?: "—", Modifier.weight(1f))
        }

        if (state.isLoading && data.incidents.isEmpty()) {
            LoadingState()
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!state.isLoading && incidents.isEmpty()) {
                EmptyMessage(pageText.empty, modifier = Modifier.padding(16.dp))
            }
            incidents.forEachIndexed { index, incident ->
                IncidentRow(
                    strings = strings,
                    pageText = pageText,
                    incident = incident,
                    onClick = { onOpenIncident(incident.id) }
                )
                if (index < incidents.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/** Campo "Date range" del diseno; abre un selector de rango y se puede limpiar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeField(
    pageText: PageText,
    from: LocalDate?,
    to: LocalDate?,
    onChanged: (LocalDate?, LocalDate?) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val hasRange = from != null && to != null

    Box {
        OutlinedTextField(
            value = if (from != null && to != null) formatRange(from, to, pageText.es) else pageText.allDates,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(pageText.dateRange) },
            trailingIcon = {
                if (hasRange) {
                    IconButton(onClick = { onChanged(null, null) }) {
                        Icon(Icons.Outlined.Close, contentDescription = pageText.clear)
                    }
                } else {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = pageText.pickRange)
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente: tocar el campo abre el selector (sin enfocar el texto).
        // Deja libre el borde derecho para que la ✕ siga funcionando.
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = if (hasRange) 56.dp else 0.dp)
                .clickable { open = true }
        )
    }

    if (open) {
        // El selector trabaja en milisegundos UTC (medianoche de cada dia)
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = from?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            initialSelectedEndDateMillis = to?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null,
                    onClick = {
                        val start = pickerState.selectedStartDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        // Si solo se eligio un dia, el rango es ese dia
                        val end = pickerState.selectedEndDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        } ?: start
                        onChanged(start, end)
                        open = false
                    }
                ) { Text(pageText.apply) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(pageText.cancel) }
            }
        ) {
            DateRangePicker(state = pickerState, modifier = Modifier.weight(1f))
        }
    }
}

/** Chip "All zones ▾" con menu desplegable. */
@Composable
private fun ZoneChip(
    pageText: PageText,
    strings: Strings,
    zones: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(selected?.let { trZone(strings, it) } ?: pageText.allZones) },
            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
            shape = RoundedCornerShape(50)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(pageText.allZones) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            zones.forEach { zone ->
                DropdownMenuItem(
                    text = { Text(trZone(strings, zone)) },
                    onClick = {
                        onSelected(zone)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Tarjeta pequena de metrica ("Incidents 12"). */
@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Fila del historial: icono, tipo, zona · fecha y estado. Abre el detalle (diseno 13). */
@Composable
private fun IncidentRow(strings: Strings, pageText: PageText, incident: Incident, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Blue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.History, contentDescription = null, tint = Blue)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                trIncidentType(strings, incident.type),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${trZone(strings, incident.zoneName)} · ${formatListDateTime(incident.detectedAt, strings.isSpanish)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SoftPill(statusLabel(strings, pageText, incident.status), statusColor(incident.status))
    }
}
