package com.smartgas_frontend.shared.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.kitchenmonitoring.presentation.components.zoneIcon
import com.smartgas_frontend.kitchenmonitoring.presentation.components.zoneStatusColor
import com.smartgas_frontend.shared.domain.model.HomeSummary
import com.smartgas_frontend.shared.domain.model.Weather
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.SoftPill
import com.smartgas_frontend.shared.presentation.navigation.Routes
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.parseDate
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Orange
import com.smartgas_frontend.ui.theme.Red
import com.smartgas_frontend.ui.theme.TagSecondary
import com.smartgas_frontend.ui.theme.TagSuccess
import com.smartgas_frontend.ui.theme.TagWarning
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val NBSP = '\u00A0'
private const val WORD_JOINER = '\u2060'

/** Cuantas zonas se muestran en el Home (el resto con "See all"). */
private const val HOME_ZONES = 3

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(val es: Boolean) {
    val goodMorning = if (es) "Buenos días," else "Good morning,"
    val goodAfternoon = if (es) "Buenas tardes," else "Good afternoon,"
    val goodEvening = if (es) "Buenas noches," else "Good evening,"
    val scanQr = if (es) "Escanear QR de sensor" else "Scan sensor QR"
    val profile = if (es) "Perfil" else "Profile"
    val overallStatus = if (es) "Estado general" else "Overall status"
    val allSafe = if (es) "Todas las zonas seguras" else "All zones safe"
    val attention = if (es) "Requiere atención" else "Attention needed"
    val critical = if (es) "Alerta crítica" else "Critical alert"
    val lastKnown = if (es) "Último estado" else "Last known"
    val zones = if (es) "Zonas" else "Zones"
    val sensors = if (es) "Sensores" else "Sensors"
    val activeAlerts = if (es) "Alertas activas" else "Active alerts"
    val seeAll = if (es) "Ver todas" else "See all"
    val noZones = if (es) "Aún no tienes zonas." else "You have no zones yet."
    val outside = if (es) "Exterior · Lima (Open-Meteo)" else "Outside · Lima (Open-Meteo)"
    val humidity = if (es) "Humedad" else "Humidity"
    val wind = if (es) "Viento" else "Wind"
    val offline = if (es) "Sin conexión · mostrando datos guardados a las" else "You are offline · showing data saved at"
    val apiError = if (es) "No se pudieron cargar los datos." else "Data could not be loaded."

    fun greeting(): String = when (LocalTime.now().hour) {
        in 5..11 -> goodMorning
        in 12..18 -> goodAfternoon
        else -> goodEvening
    }

    fun sensorsOnline(zones: Int, online: Int) =
        if (es) "$zones zonas · $online sensores en línea" else "$zones zones · $online sensors online"

    /** "updated 5 s ago" / "actualizado hace 2 min" */
    fun updatedAgo(seconds: Long): String = when {
        seconds < 60 -> if (es) "actualizado hace $seconds s" else "updated $seconds s ago"
        else -> if (es) "actualizado hace ${seconds / 60} min" else "updated ${seconds / 60} min ago"
    }

    fun updatedAt(time: String) = if (es) "actualizado $time" else "updated $time"
}

private fun clockTime(isoString: String?): String =
    parseDate(isoString)?.atZone(ZoneId.systemDefault())?.let { DateTimeFormatter.ofPattern("HH:mm").format(it) } ?: "—"

/** Diseno 04 (Home) y 17 (Home sin conexion). */
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigate: (String) -> Unit, onOpenZone: (Int) -> Unit) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val state = viewModel.state.value
    val weather = viewModel.weatherState.value.data
    val data = state.data
    val updatedAt = viewModel.updatedAt.value

    // Actualizacion automatica cada 15 s mientras el Home esta en pantalla
    LaunchedEffect(Unit) {
        viewModel.loadData()
        while (true) {
            delay(HOME_REFRESH_MILLIS)
            viewModel.loadData(silent = true)
        }
    }

    // Reloj de 1 s para que "updated 5 s ago" avance solo
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1_000)
            value = System.currentTimeMillis()
        }
    }

    Column {
        // Diseno 17: franja superior cuando los datos vienen de la copia local
        if (data?.offlineSavedAt != null) {
            OfflineBanner("${pageText.offline} ${clockTime(data.offlineSavedAt)}")
        }

        ContentPage {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pageText.greeting(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        viewModel.firstName.ifEmpty { "SmartGas" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                // Icono QR del diseno: abre Add sensor con el escaner (diseno 09)
                IconButton(onClick = { onNavigate(Routes.addSensor(scan = true)) }) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = pageText.scanQr)
                }
                IconButton(onClick = { onNavigate(Routes.PROFILE) }) {
                    Icon(Icons.Outlined.AccountCircle, contentDescription = pageText.profile)
                }
            }

            if (data == null) {
                if (state.isLoading) LoadingState() else EmptyMessage(pageText.apiError)
                return@ContentPage
            }

            val status = effectiveOverallStatus(data)
            val onlineSensors = data.sensors.count { normalizeStatus(it.status) != "offline" }
            val secondsAgo = ((now - updatedAt) / 1000).coerceAtLeast(0)

            OverallStatusCard(
                pageText = pageText,
                status = status,
                offline = data.offlineSavedAt != null,
                detail = buildString {
                    append(pageText.sensorsOnline(data.zones.size, onlineSensors))
                    append(" · ")
                    append(
                        if (data.offlineSavedAt != null || updatedAt == 0L) pageText.updatedAt(clockTime(data.offlineSavedAt))
                        else pageText.updatedAgo(secondsAgo)
                    )
                },
                statusLabel = trStatus(strings, status)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(pageText.zones, data.zones.size.toString(), modifier = Modifier.weight(1f))
                StatCard(pageText.sensors, data.sensors.size.toString(), modifier = Modifier.weight(1f))
                StatCard(
                    pageText.activeAlerts,
                    data.pendingAlerts.size.toString(),
                    valueColor = if (data.pendingAlerts.isEmpty()) TagSuccess else Red,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pageText.zones,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onNavigate(Routes.ZONES) }) {
                    Text(pageText.seeAll, color = Blue)
                }
            }

            if (data.zones.isEmpty()) {
                EmptyMessage(pageText.noZones)
            }

            // Primero las zonas con problemas, para que se vean sin bajar
            data.zones
                .sortedBy { zoneOrder(it.status) }
                .take(HOME_ZONES)
                .forEach { zone ->
                    ZoneRow(strings = strings, zone = zone, onClick = { onOpenZone(zone.id) })
                }

            if (weather != null) {
                WeatherCard(pageText, weather)
            }
        }
    }
}

private fun zoneOrder(status: String?): Int = when (normalizeStatus(status)) {
    "critical" -> 0
    "warning" -> 1
    else -> 2
}

@Composable
private fun OfflineBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF37474F))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = Color.White)
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Tarjeta grande: verde si todo esta seguro, naranja advertencia, rojo critico y gris sin conexion. */
@Composable
private fun OverallStatusCard(pageText: PageText, status: String, offline: Boolean, detail: String, statusLabel: String) {
    val normalized = normalizeStatus(status)
    val color = when {
        offline -> TagSecondary
        normalized == "critical" -> Red
        normalized == "warning" -> TagWarning
        else -> TagSuccess
    }
    val title = when {
        offline -> "${pageText.lastKnown}: $statusLabel"
        normalized == "critical" -> pageText.critical
        normalized == "warning" -> pageText.attention
        else -> pageText.allSafe
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(pageText.overallStatus, color = Color.White, style = MaterialTheme.typography.bodySmall)
                Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(detail, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                if (normalized == "safe") Icons.Outlined.GppGood else Icons.Outlined.ReportProblem,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color? = null) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Fila "Main kitchen · Gas 18 ppm · Temp 34 °C · SAFE". */
@Composable
private fun ZoneRow(strings: Strings, zone: Zone, onClick: () -> Unit) {
    val readings = listOfNotNull(
        zone.gasLevel?.let { "Gas ${formatNumber(it)} ppm" },
        zone.temperature?.let { "Temp ${formatNumber(it)} °C" }
    ).joinToString(" · ")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconTile(zoneIcon(zone.name), Blue)
            Column(modifier = Modifier.weight(1f)) {
                Text(trZone(strings, zone.name), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (readings.isNotEmpty()) {
                    Text(readings, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            SoftPill(trStatus(strings, zone.status), zoneStatusColor(zone.status))
        }
    }
}

/** "Outside · Lima (Open-Meteo) — 19 °C · Humidity 82 % · Wind 11 km/h". */
@Composable
private fun WeatherCard(pageText: PageText, weather: Weather) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconTile(Icons.Outlined.WbSunny, Orange)
            Column {
                Text(pageText.outside, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    // Espacios y barra "no separables" para que "11.2 km/h" no se parta en dos lineas
                    listOf(
                        "${formatNumber(weather.temperature)}$NBSP${weather.temperatureUnit}",
                        "${pageText.humidity}$NBSP${formatNumber(weather.relativeHumidity)}$NBSP${weather.relativeHumidityUnit}",
                        "${pageText.wind}$NBSP${formatNumber(weather.windSpeed)}$NBSP${weather.windSpeedUnit.replace("/", "$WORD_JOINER/$WORD_JOINER")}"
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun IconTile(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color)
    }
}
