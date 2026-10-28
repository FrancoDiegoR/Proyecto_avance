package com.smartgas_frontend.kitchenmonitoring.presentation.zones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.kitchenmonitoring.data.repository.ZoneDraft
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.shared.data.remote.UNLIMITED
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DropdownField
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.ListRow
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PlanLimitBanner
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.TextInput
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.presentation.navigation.Routes
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.trPlanName
import com.smartgas_frontend.shared.utils.trSensorType
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.TagDanger
import com.smartgas_frontend.ui.theme.TagWarning

private fun statusSeverity(status: String?): TagSeverity = when (status) {
    "Safe" -> TagSeverity.Success
    "Warning" -> TagSeverity.Warning
    "Critical" -> TagSeverity.Danger
    "Offline" -> TagSeverity.Secondary
    else -> TagSeverity.Info
}

private fun riskSeverity(risk: String?): TagSeverity = when (risk) {
    "Low" -> TagSeverity.Success
    "Medium" -> TagSeverity.Warning
    "High", "Critical" -> TagSeverity.Danger
    else -> TagSeverity.Info
}

@Composable
fun ZonesScreen(viewModel: ZonesViewModel, onNavigate: (String) -> Unit, onOpenZone: (Int) -> Unit = {}) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val data = state.data ?: MonitoringData()
    val loadingData = state.isLoading
    val activeFilter = viewModel.activeFilter.value
    val zoneDraft = viewModel.zoneDraft.value
    val zoneSaveLoading = viewModel.zoneSaveLoading.value
    val currentPlan = data.currentPlan

    val maxZonesLabel = if (data.unlimitedZones) strings.t(R.string.unlimitedShort) else data.maxZones.toString()
    val remainingZones = if (data.unlimitedZones) strings.t(R.string.unlimitedShort) else data.remainingZones.toString()
    val planLimitText = strings.t(
        R.string.zonePlanLimitText,
        "plan" to trPlanName(strings, currentPlan),
        "max" to maxZonesLabel
    )

    val sensitivityOptions = listOf(
        strings.t(R.string.high) to "High",
        strings.t(R.string.medium) to "Medium",
        strings.t(R.string.low) to "Low"
    )

    ToastEffect(viewModel.toasts)

    ContentPage {
        PageHeader(title = strings.t(R.string.monitoringTitle), subtitle = strings.t(R.string.monitoringSubtitle)) {
            SecondaryButton(
                label = strings.t(R.string.refreshReadings),
                icon = Icons.Outlined.Refresh,
                loading = loadingData,
                onClick = viewModel::loadData
            )
            SecondaryButton(
                label = strings.t(R.string.addZone),
                icon = Icons.Outlined.Add,
                enabled = !loadingData && data.canAddZone,
                onClick = viewModel::openZoneDialog
            )
            PrimaryButton(
                label = strings.t(R.string.simulateReading),
                icon = Icons.Outlined.Bolt,
                enabled = data.hasSensors,
                onClick = viewModel::openSimulate
            )
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (!loadingData) {
            when {
                data.zoneLimitExceeded ->
                    PlanLimitBanner(strings.t(R.string.zoneLimitExceeded, "max" to maxZonesLabel), warning = true)
                data.hasZones && data.zoneLimitReached ->
                    PlanLimitBanner(strings.t(R.string.zoneLimitReachedInfo, "max" to maxZonesLabel))
                currentPlan != null -> PlanLimitBanner(planLimitText)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "All" to R.string.allZones,
                "Safe" to R.string.filterSafe,
                "Warning" to R.string.filterWarning,
                "Critical" to R.string.filterCritical,
                "Offline" to R.string.filterOffline
            ).forEach { (filter, label) ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { viewModel.onFilterChanged(filter) },
                    label = { Text(strings.t(label)) }
                )
            }
        }

        if (loadingData) {
            LoadingState()
        }

        if (!loadingData && !data.hasSensors) {
            ZoneSetupState(
                strings = strings,
                data = data,
                maxZonesLabel = maxZonesLabel,
                remainingZones = remainingZones,
                planLimitText = planLimitText,
                zoneDraft = zoneDraft,
                sensitivityOptions = sensitivityOptions,
                zoneSaveLoading = zoneSaveLoading,
                onZoneDraftChanged = viewModel::onZoneDraftChanged,
                onSaveZone = viewModel::saveZone,
                onAddDevice = { onNavigate(Routes.SENSORS) }
            )
        }

        if (!loadingData && data.hasSensors) {
            viewModel.filteredZones.forEach { zone ->
                ZoneMonitorCard(
                    strings = strings,
                    zone = zone,
                    sensorsConnected = data.sensorsForZone(zone.id),
                    onClick = { onOpenZone(zone.id) }
                )
            }

            if (viewModel.filteredZones.isEmpty()) {
                EmptyMessage(strings.t(R.string.noData))
            }
        }
    }

    if (viewModel.showZoneDialog.value) {
        AlertDialog(
            onDismissRequest = viewModel::closeZoneDialog,
            title = { Text(strings.t(R.string.addZoneTitle)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.t(R.string.createZoneHint), color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (currentPlan != null) {
                        PlanLimitBanner(planLimitText)
                    }

                    if (data.canAddZone) {
                        ZoneDraftFields(strings, zoneDraft, sensitivityOptions, viewModel::onZoneDraftChanged)
                    } else {
                        ZoneLimitCard(strings.t(R.string.zoneLimitReachedDetail, "max" to maxZonesLabel))
                    }
                }
            },
            dismissButton = {
                SecondaryButton(label = strings.t(R.string.cancelAction), onClick = viewModel::closeZoneDialog)
            },
            confirmButton = {
                PrimaryButton(
                    label = strings.t(R.string.saveNewZone),
                    icon = Icons.Outlined.Check,
                    loading = zoneSaveLoading,
                    enabled = data.canAddZone,
                    onClick = viewModel::saveZone
                )
            }
        )
    }

    if (viewModel.showSimulateDialog.value) {
        val simSensorId = viewModel.simSensorId.value
        val zoneForSensor = viewModel.zoneForSensor
        val sensorOptions = viewModel.activeSensors.map { sensor ->
            "${sensor.code} – ${sensor.name} (${trSensorType(strings, sensor.type)})" to sensor.id
        }

        AlertDialog(
            onDismissRequest = viewModel::closeSimulate,
            title = { Text(strings.t(R.string.simulateTitle)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.t(R.string.simulateSubtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)

                    DropdownField(
                        label = strings.t(R.string.selectSensor),
                        options = sensorOptions,
                        selected = simSensorId ?: -1,
                        onSelected = viewModel::onSimSensorSelected,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (zoneForSensor != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Tag(value = trZone(strings, zoneForSensor.name), severity = TagSeverity.Info)
                            Tag(
                                value = trStatus(strings, zoneForSensor.status),
                                severity = statusSeverity(zoneForSensor.status)
                            )
                        }
                    }

                    TextInput(
                        label = strings.t(R.string.gasValueLabel),
                        value = viewModel.simGas.value,
                        onValueChange = viewModel::onSimGasChanged,
                        keyboardType = KeyboardType.Number,
                        enabled = viewModel.allowsGasValue
                    )
                    TextInput(
                        label = strings.t(R.string.tempValueLabel),
                        value = viewModel.simTemp.value,
                        onValueChange = viewModel::onSimTempChanged,
                        keyboardType = KeyboardType.Number,
                        enabled = viewModel.allowsTemperatureValue
                    )

                    SecondaryButton(
                        label = strings.t(R.string.randomizeValues),
                        icon = Icons.Outlined.Shuffle,
                        onClick = viewModel::randomize
                    )
                }
            },
            dismissButton = {
                SecondaryButton(label = strings.t(R.string.cancelAction), onClick = viewModel::closeSimulate)
            },
            confirmButton = {
                PrimaryButton(
                    label = strings.t(R.string.processReading),
                    icon = Icons.Outlined.Bolt,
                    loading = viewModel.simLoading.value,
                    enabled = simSensorId != null,
                    onClick = viewModel::processReading
                )
            }
        )
    }
}

@Composable
private fun ZoneDraftFields(
    strings: Strings,
    zoneDraft: ZoneDraft,
    sensitivityOptions: List<Pair<String, String>>,
    onZoneDraftChanged: (ZoneDraft) -> Unit
) {
    TextInput(
        label = strings.t(R.string.zoneName),
        value = zoneDraft.name,
        onValueChange = { onZoneDraftChanged(zoneDraft.copy(name = it)) },
        placeholder = strings.t(R.string.zoneNamePlaceholder)
    )
    DropdownField(
        label = strings.t(R.string.zoneSensitivity),
        options = sensitivityOptions,
        selected = zoneDraft.sensitivity,
        onSelected = { onZoneDraftChanged(zoneDraft.copy(sensitivity = it)) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ZoneLimitCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

/** .monitoring-empty-state: guia inicial cuando todavia no hay sensores. */
@Composable
private fun ZoneSetupState(
    strings: Strings,
    data: MonitoringData,
    maxZonesLabel: String,
    remainingZones: String,
    planLimitText: String,
    zoneDraft: ZoneDraft,
    sensitivityOptions: List<Pair<String, String>>,
    zoneSaveLoading: Boolean,
    onZoneDraftChanged: (ZoneDraft) -> Unit,
    onSaveZone: () -> Unit,
    onAddDevice: () -> Unit
) {
    val hasZones = data.hasZones

    PanelCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasZones) Icons.Outlined.Memory else Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                strings.t(if (hasZones) R.string.monitoringNoSensorsTitle else R.string.monitoringEmptyTitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                strings.t(if (hasZones) R.string.monitoringNoSensorsText else R.string.monitoringEmptyText),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (data.currentPlan != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(planLimitText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (hasZones) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.t(R.string.createdZonesTitle), style = MaterialTheme.typography.titleMedium)
                Text("${data.zones.size} / $maxZonesLabel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            data.zones.forEach { zone ->
                ListRow {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(trZone(strings, zone.name), fontWeight = FontWeight.SemiBold)
                        Text(
                            "${strings.t(R.string.zoneSensitivity)}: ${trSeverity(strings, zone.sensitivity)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Tag(value = trStatus(strings, zone.status), severity = statusSeverity(zone.status))
                }
            }
        }

        if (data.canAddZone) {
            Text(
                strings.t(if (hasZones) R.string.addAnotherZone else R.string.createFirstZone),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                strings.t(R.string.createZoneHint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ZoneDraftFields(strings, zoneDraft, sensitivityOptions, onZoneDraftChanged)

            Text(
                strings.t(R.string.remainingZonesText, "count" to remainingZones),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PrimaryButton(
                label = strings.t(if (hasZones) R.string.saveNewZone else R.string.saveFirstZone),
                icon = Icons.Outlined.Check,
                loading = zoneSaveLoading,
                onClick = onSaveZone
            )
        } else {
            ZoneLimitCard(strings.t(R.string.zoneLimitReachedDetail, "max" to maxZonesLabel))
        }

        listOf(
            R.string.monitoringStepCreateZones,
            R.string.monitoringStepAddSensor,
            R.string.monitoringStepSimulate,
            R.string.monitoringStepReview
        ).forEachIndexed { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(strings.t(step), style = MaterialTheme.typography.bodyMedium)
            }
        }

        SecondaryButton(
            label = strings.t(R.string.addFirstDevice),
            icon = Icons.Outlined.Add,
            enabled = hasZones,
            onClick = onAddDevice
        )
    }
}

/** .zone-monitor-card */
@Composable
private fun ZoneMonitorCard(strings: Strings, zone: Zone, sensorsConnected: Int, onClick: () -> Unit) {
    val zoneName = trZone(strings, zone.name)
    val zoneStatus = trStatus(strings, zone.status)
    val border = when (zone.status) {
        "Warning" -> TagWarning
        "Critical" -> TagDanger
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$zoneName – $zoneStatus" },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    zoneName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                Tag(value = zoneStatus, severity = statusSeverity(zone.status))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingItem(
                    Icons.Outlined.LocalFireDepartment,
                    strings.t(R.string.gasLevel),
                    "${formatNumber(zone.gasLevel)} ppm",
                    Modifier.weight(1f)
                )
                ReadingItem(
                    Icons.Outlined.WbSunny,
                    strings.t(R.string.temperature),
                    "${formatNumber(zone.temperature)}°C",
                    Modifier.weight(1f)
                )
                ReadingItem(
                    Icons.Outlined.Wifi,
                    strings.t(R.string.sensorsConnected),
                    sensorsConnected.toString(),
                    Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tag(
                    value = "${strings.t(R.string.riskLevel)}: ${trSeverity(strings, zone.riskLevel.ifEmpty { "Low" })}",
                    severity = riskSeverity(zone.riskLevel)
                )
                Text(
                    formatDate(zone.lastUpdated),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReadingItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
