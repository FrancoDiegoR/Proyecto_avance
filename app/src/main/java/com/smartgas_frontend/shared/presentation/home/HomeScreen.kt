package com.smartgas_frontend.shared.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.shared.domain.model.HomeSummary
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.ListRow
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.MetricCard
import com.smartgas_frontend.shared.presentation.components.MetricTone
import com.smartgas_frontend.shared.presentation.components.MetricsGrid
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.navigation.Routes
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trPlanDescription
import com.smartgas_frontend.shared.utils.trPlanName
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.TagDanger
import com.smartgas_frontend.ui.theme.TagSuccess
import com.smartgas_frontend.ui.theme.TagWarning

@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigate: (String) -> Unit) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val weatherState = viewModel.weatherState.value
    val data = state.data ?: HomeSummary()

    val isSpanish = strings.isSpanish
    val weatherTitle = if (isSpanish) "Clima externo" else "External Weather"
    val weatherTemperature = if (isSpanish) "Temperatura" else "Temperature"
    val weatherHumidity = if (isSpanish) "Humedad" else "Humidity"
    val weatherWind = if (isSpanish) "Viento" else "Wind"
    val weatherSource = if (isSpanish) "Fuente" else "Source"
    val weatherLoading = if (isSpanish) "Cargando datos del clima externo..." else "Loading external weather data..."
    val weatherUnavailable =
        if (isSpanish) "Los datos del clima externo no están disponibles." else "External weather data is not available."

    ContentPage {
        PageHeader(title = strings.t(R.string.dashboardTitle), subtitle = strings.t(R.string.dashboardSubtitle)) {
            SecondaryButton(
                label = strings.t(R.string.refreshData),
                icon = Icons.Outlined.Refresh,
                loading = state.isLoading,
                onClick = viewModel::loadData
            )
            // Icono de usuario del diseno (04 Home): abre Profile.
            IconButton(onClick = { onNavigate(Routes.PROFILE) }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = if (isSpanish) "Perfil" else "Profile")
            }
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (state.isLoading && state.message.isEmpty()) {
            LoadingState()
        }

        if (!state.isLoading) {
            val overallStatus = effectiveOverallStatus(data)

            OverallStatusCard(
                label = strings.t(R.string.overallStatus),
                value = trStatus(strings, overallStatus),
                status = overallStatus
            )

            MetricsGrid(
                listOf(
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.Wifi,
                            value = data.sensors.count { it.status != "Offline" }.toString(),
                            label = strings.t(R.string.connectedSensors),
                            small = "${data.sensors.size} ${strings.t(R.string.devices)}",
                            modifier = modifier
                        )
                    },
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.LocationOn,
                            value = data.zones.size.toString(),
                            label = strings.t(R.string.monitoredZones),
                            small = "${data.zones.count { it.status == "Safe" }} ${strings.t(R.string.statusSafe)}",
                            modifier = modifier
                        )
                    },
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.WarningAmber,
                            value = data.activeIncidents.size.toString(),
                            label = strings.t(R.string.activeIncidents),
                            small = strings.t(R.string.pending),
                            tone = if (data.activeIncidents.isNotEmpty()) MetricTone.Orange else MetricTone.Blue,
                            modifier = modifier
                        )
                    },
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.Notifications,
                            value = data.pendingAlerts.size.toString(),
                            label = strings.t(R.string.pendingAlerts),
                            small = strings.t(R.string.pending),
                            tone = if (data.pendingAlerts.isNotEmpty()) MetricTone.Red else MetricTone.Blue,
                            modifier = modifier
                        )
                    }
                )
            )

            PanelCard(title = strings.t(R.string.monitoredZones), icon = Icons.Outlined.Map) {
                data.zones.forEach { zone ->
                    ListRow(accent = zoneAccent(zone.status)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trZone(strings, zone.name), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Gas: ${formatNumber(zone.gasLevel)} ppm  |  ${strings.t(R.string.temperature)}: ${formatNumber(zone.temperature)}°C",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Tag(
                            value = trStatus(strings, zone.status),
                            severity = when (zone.status) {
                                "Safe" -> TagSeverity.Success
                                "Warning" -> TagSeverity.Warning
                                "Critical" -> TagSeverity.Danger
                                else -> TagSeverity.Secondary
                            }
                        )
                    }
                }

                if (data.zones.isEmpty()) {
                    EmptyMessage(strings.t(R.string.noData))
                }

                SecondaryButton(
                    label = strings.t(R.string.viewMonitoring),
                    icon = Icons.Outlined.Visibility,
                    onClick = { onNavigate(Routes.ZONES) }
                )
            }

            PanelCard(title = strings.t(R.string.activeIncidents), icon = Icons.Outlined.WarningAmber) {
                data.activeIncidents.take(5).forEach { incident ->
                    ListRow {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(incident.code, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "${trZone(strings, incident.zoneName)} — ${trIncidentType(strings, incident.type)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Tag(
                            value = trSeverity(strings, incident.severity),
                            severity = if (incident.severity == "Critical") TagSeverity.Danger else TagSeverity.Warning
                        )
                    }
                }

                if (data.activeIncidents.isEmpty()) {
                    ReadingRow(Icons.Outlined.CheckCircle, strings.t(R.string.noCriticalZone), tint = TagSuccess)
                }

                SecondaryButton(
                    label = strings.t(R.string.reviewIncidents),
                    icon = Icons.AutoMirrored.Outlined.List,
                    onClick = { onNavigate(Routes.HISTORY) }
                )
            }

            PanelCard(title = strings.t(R.string.currentPlan), icon = Icons.Outlined.CreditCard) {
                Text(
                    if (data.plan != null) trPlanName(strings, data.plan) else "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (data.plan != null) {
                    Text(
                        trPlanDescription(strings, data.plan),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (data.subscription != null) {
                    Tag(value = strings.t(R.string.active), severity = TagSeverity.Success)
                }

                SecondaryButton(
                    label = strings.t(R.string.subscription),
                    icon = Icons.Outlined.CreditCard,
                    onClick = { onNavigate(Routes.PLANS) }
                )
            }

            PanelCard(title = strings.t(R.string.lastReading), icon = Icons.AutoMirrored.Outlined.ShowChart) {
                val lastReading = data.lastReading

                if (lastReading != null) {
                    ReadingRow(Icons.Outlined.LocalFireDepartment, "Gas: ", "${formatNumber(lastReading.gasValue)} ppm")
                    ReadingRow(
                        Icons.Outlined.WbSunny,
                        "${strings.t(R.string.temperature)}: ",
                        "${formatNumber(lastReading.temperatureValue)}°C"
                    )
                    ReadingRow(Icons.Outlined.Schedule, formatDate(lastReading.timestamp))
                } else {
                    EmptyMessage(strings.t(R.string.noData))
                }

                SecondaryButton(
                    label = strings.t(R.string.viewReports),
                    icon = Icons.Outlined.BarChart,
                    onClick = { onNavigate(Routes.REPORTS) }
                )
            }

            PanelCard(title = weatherTitle, icon = Icons.Outlined.Cloud) {
                val weather = weatherState.data

                when {
                    weatherState.isLoading -> LoadingRow(weatherLoading)
                    weather != null -> {
                        ReadingRow(
                            Icons.Outlined.WbSunny,
                            "$weatherTemperature: ",
                            "${formatNumber(weather.temperature)} ${weather.temperatureUnit}"
                        )
                        ReadingRow(
                            Icons.Outlined.Cloud,
                            "$weatherHumidity: ",
                            "${formatNumber(weather.relativeHumidity)} ${weather.relativeHumidityUnit}"
                        )
                        ReadingRow(
                            Icons.Outlined.Explore,
                            "$weatherWind: ",
                            "${formatNumber(weather.windSpeed)} ${weather.windSpeedUnit}"
                        )
                        ReadingRow(Icons.Outlined.Storage, "$weatherSource: ${weather.source}")
                    }
                    else -> EmptyMessage(weatherUnavailable)
                }
            }
        }
    }
}

@Composable
private fun zoneAccent(status: String): Color? = when (status) {
    "Warning" -> TagWarning
    "Critical" -> TagDanger
    else -> null
}

@Composable
private fun OverallStatusCard(label: String, value: String, status: String) {
    val (color, icon) = when (status) {
        "Warning" -> TagWarning to Icons.Outlined.WarningAmber
        "Critical" -> TagDanger to Icons.Outlined.Cancel
        else -> TagSuccess to Icons.Outlined.CheckCircle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .semantics(mergeDescendants = true) { contentDescription = "Overall status: $value" }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun ReadingRow(icon: ImageVector, label: String, value: String? = null, tint: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = tint ?: MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Row {
            Text(label, color = tint ?: MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            if (value != null) {
                Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LoadingRow(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
}
