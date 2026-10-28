package com.smartgas_frontend.kitchenmonitoring.presentation.sensordetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.kitchenmonitoring.presentation.components.GasSeriesColor
import com.smartgas_frontend.kitchenmonitoring.presentation.components.ReadingsChart
import com.smartgas_frontend.kitchenmonitoring.presentation.components.TemperatureSeriesColor
import com.smartgas_frontend.kitchenmonitoring.presentation.components.formatReadingTime
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DetailRow
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.statusClass
import com.smartgas_frontend.shared.utils.trSensorName
import com.smartgas_frontend.shared.utils.trSensorType
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(es: Boolean) {
    val back = if (es) "Volver" else "Back"
    val refresh = if (es) "Actualizar" else "Refresh"
    val edit = if (es) "Editar" else "Edit"
    val gas = "Gas"
    val temperature = if (es) "Temperatura" else "Temperature"
    val gasChart = if (es) "Nivel de gas · 24 h" else "Gas level · 24 h"
    val temperatureChart = if (es) "Temperatura · 24 h" else "Temperature · 24 h"
    val threshold = if (es) "umbral" else "threshold"
    val battery = if (es) "Batería" else "Battery"
    val lastReading = if (es) "Última lectura" else "Last reading"
    val noReadings = if (es) "Aún no hay lecturas de este sensor." else "No readings for this sensor yet."
    val notFound = if (es) "No se encontró el sensor." else "Sensor not found."
}

@Composable
fun SensorDetailScreen(viewModel: SensorDetailViewModel, onBack: () -> Unit, onEdit: (Int) -> Unit) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val state = viewModel.state.value
    val data = state.data ?: SensorDetailData()
    val sensor = data.sensor
    val latest = data.latestReading

    // Se recarga cada vez que la pantalla vuelve a mostrarse.
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    ContentPage {
        // Barra superior del diseno 07: flecha atras + codigo del sensor
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = pageText.back)
            }
            Text(
                text = sensor?.code.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            IconButton(onClick = viewModel::loadData, enabled = !state.isLoading) {
                Icon(Icons.Outlined.Refresh, contentDescription = pageText.refresh)
            }
            // Lapiz del diseno 07
            if (sensor != null) {
                IconButton(onClick = { onEdit(sensor.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = pageText.edit)
                }
            }
        }

        if (state.isLoading && sensor == null) {
            LoadingState()
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(if (sensor == null) pageText.notFound else strings.t(R.string.apiError))
        }

        if (sensor != null) {
            // Lectura actual: gas y/o temperatura segun el tipo de sensor
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (data.measuresGas) {
                    ReadingCard(
                        label = pageText.gas,
                        value = formatNumber(latest?.gasValue),
                        unit = "ppm",
                        color = GasSeriesColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (data.measuresTemperature) {
                    ReadingCard(
                        label = pageText.temperature,
                        value = formatNumber(latest?.temperatureValue),
                        unit = "°C",
                        color = TemperatureSeriesColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            PanelCard(title = if (data.measuresGas) pageText.gasChart else pageText.temperatureChart) {
                if (data.chartReadings.size < 2) {
                    EmptyMessage(pageText.noReadings)
                } else {
                    ReadingsChart(
                        readings = data.chartReadings,
                        threshold = data.gasThreshold,
                        thresholdLabel = "${pageText.threshold} ${formatNumber(data.gasThreshold)}",
                        showGas = data.measuresGas,
                        showTemperature = !data.measuresGas
                    )
                }
            }

            PanelCard {
                Column {
                    DetailRow(strings.t(R.string.sensorName), trSensorName(strings, sensor.name))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(strings.t(R.string.sensorType), trSensorType(strings, sensor.type))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(strings.t(R.string.zone), data.zone?.let { trZone(strings, it.name) } ?: "—")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(pageText.battery, "${sensor.battery} %")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(pageText.lastReading, formatReadingTime(latest?.timestamp ?: sensor.lastConnected))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(strings.t(R.string.status)) {
                        Tag(value = trStatus(strings, sensor.status), severity = statusClass(sensor.status))
                    }
                }
            }
        }
    }
}

/** Tarjeta pequena del diseno: "Gas (LP) 41 %LEL" / "Temperature 27 °C". */
@Composable
private fun ReadingCard(label: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                Text(unit, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}
