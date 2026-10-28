package com.smartgas_frontend.kitchenmonitoring.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.kitchenmonitoring.domain.model.SensorReading
import com.smartgas_frontend.shared.utils.dateMillis
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.parseDate
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Orange
import com.smartgas_frontend.ui.theme.TagDanger
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Componentes de presentacion compartidos por las pantallas de kitchenmonitoring
// (Zone detail y Sensor detail usan el mismo grafico).

/** Colores de las series: gas en naranja y temperatura en azul, como en el diseno. */
val GasSeriesColor = Orange
val TemperatureSeriesColor = Blue

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Hora corta (HH:mm) de una lectura en la zona horaria del telefono. */
fun formatReadingTime(isoString: String?): String {
    val instant = parseDate(isoString) ?: return "—"
    return timeFormatter.format(instant.atZone(ZoneId.systemDefault()))
}

/**
 * Lecturas de las ultimas [hours] horas, contadas desde la lectura mas reciente
 * (no desde "ahora", para que tambien se vean datos antiguos como los del mock).
 */
fun List<SensorReading>.withinLastHours(hours: Int): List<SensorReading> {
    val sorted = sortedBy { dateMillis(it.timestamp) }
    val latest = sorted.lastOrNull()?.let { dateMillis(it.timestamp) } ?: return emptyList()
    val windowStart = latest - hours * 60L * 60L * 1000L
    return sorted.filter { dateMillis(it.timestamp) >= windowStart }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Grafico de lineas de gas y/o temperatura con la linea punteada del umbral de gas.
 * Se dibuja con Canvas para no agregar una libreria de graficos al proyecto.
 * [readings] debe venir ordenado por fecha (usar [withinLastHours]).
 */
@Composable
fun ReadingsChart(
    readings: List<SensorReading>,
    threshold: Double,
    thresholdLabel: String,
    showGas: Boolean = true,
    showTemperature: Boolean = true
) {
    val values = readings.flatMap {
        listOfNotNull(
            it.gasValue.takeIf { showGas },
            it.temperatureValue.takeIf { showTemperature }
        )
    }
    val maxY = maxOf(100.0, threshold * 1.2, values.maxOrNull() ?: 0.0)

    val times = readings.map { dateMillis(it.timestamp) }
    val minTime = times.first()
    val maxTime = times.last()

    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            thresholdLabel,
            style = MaterialTheme.typography.bodySmall,
            color = TagDanger,
            modifier = Modifier.align(Alignment.End)
        )

        Row(modifier = Modifier.height(160.dp)) {
            // Etiquetas del eje Y
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(maxY, maxY / 2, 0.0).forEach {
                    Text(formatNumber(it.toInt().toDouble()), style = MaterialTheme.typography.labelSmall, color = labelColor)
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                fun x(index: Int): Float {
                    val span = (maxTime - minTime).toFloat()
                    return if (span <= 0f) {
                        size.width * index / (readings.size - 1).coerceAtLeast(1)
                    } else {
                        size.width * (times[index] - minTime) / span
                    }
                }

                fun y(value: Double): Float = size.height - (size.height * (value / maxY)).toFloat()

                // Lineas guia horizontales (0, mitad, maximo)
                listOf(0.0, maxY / 2, maxY).forEach { level ->
                    drawLine(gridColor, Offset(0f, y(level)), Offset(size.width, y(level)), strokeWidth = 1f)
                }

                // Umbral de gas (linea roja punteada)
                drawLine(
                    color = TagDanger,
                    start = Offset(0f, y(threshold)),
                    end = Offset(size.width, y(threshold)),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                )

                fun drawSeries(color: Color, valueOf: (SensorReading) -> Double?) {
                    val path = Path()
                    var started = false

                    readings.forEachIndexed { index, reading ->
                        val value = valueOf(reading) ?: return@forEachIndexed
                        if (!started) {
                            path.moveTo(x(index), y(value))
                            started = true
                        } else {
                            path.lineTo(x(index), y(value))
                        }
                    }

                    if (started) {
                        drawPath(path, color, style = Stroke(width = 5f))
                    }
                }

                if (showTemperature) drawSeries(TemperatureSeriesColor) { it.temperatureValue }
                if (showGas) drawSeries(GasSeriesColor) { it.gasValue }
            }
        }

        // Etiquetas del eje X (inicio, mitad y fin del rango)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(readings.first(), readings[readings.size / 2], readings.last()).forEach {
                Text(formatReadingTime(it.timestamp), style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
        }
    }
}
