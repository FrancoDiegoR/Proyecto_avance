package com.smartgas_frontend.kitchenmonitoring.presentation.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DetailRow
import com.smartgas_frontend.shared.presentation.components.DropdownField
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PlanLimitBanner
import com.smartgas_frontend.shared.presentation.components.FormError
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.TextInput
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.trLocationDetail
import com.smartgas_frontend.shared.utils.trSensorName
import com.smartgas_frontend.shared.utils.trSensorType
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.TagSuccess
import com.smartgas_frontend.ui.theme.TagWarning

private fun statusSeverity(status: String?): TagSeverity = when (status) {
    "Online" -> TagSeverity.Success
    "Offline" -> TagSeverity.Secondary
    "Warning" -> TagSeverity.Warning
    "Critical" -> TagSeverity.Danger
    else -> TagSeverity.Info
}

@Composable
fun SensorsScreen(viewModel: SensorsViewModel) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val data = state.data ?: DevicesData()
    val loading = state.isLoading
    val editingId = viewModel.editingId.value
    val form = viewModel.form.value
    val formError = viewModel.formError.value
    val selectedSensor = viewModel.selectedSensor.value

    val maxSensorsLabel = if (data.unlimitedSensors) strings.t(R.string.unlimitedShort) else data.maxSensors.toString()
    val zoneName: (Int?) -> String = { zoneId -> trZone(strings, data.zones.find { it.id == zoneId }?.name) }

    ToastEffect(viewModel.toasts)

    ContentPage {
        PageHeader(title = strings.t(R.string.devicesTitle), subtitle = strings.t(R.string.devicesSubtitle)) {
            PrimaryButton(
                label = strings.t(R.string.addDevice),
                icon = Icons.Outlined.Add,
                enabled = data.zones.isNotEmpty() && data.canAddSensor,
                onClick = viewModel::openAdd
            )
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (!loading && data.zones.isEmpty()) {
            PlanLimitBanner(strings.t(R.string.createZoneBeforeDevice))
        }

        if (!loading) {
            when {
                data.sensorLimitExceeded ->
                    PlanLimitBanner(strings.t(R.string.sensorLimitExceeded, "max" to maxSensorsLabel), warning = true)
                data.sensorLimitReached ->
                    PlanLimitBanner(strings.t(R.string.sensorLimitReachedInfo, "max" to maxSensorsLabel))
                data.currentPlan != null ->
                    PlanLimitBanner(strings.t(R.string.sensorPlanLimitText, "max" to maxSensorsLabel))
            }
        }

        if (loading) {
            LoadingState()
        }

        if (!loading) {
            data.sensors.forEach { sensor ->
                SensorCard(
                    strings = strings,
                    sensor = sensor,
                    zoneName = zoneName(sensor.zoneId),
                    onDetail = { viewModel.openDetail(sensor) },
                    onEdit = { viewModel.openEdit(sensor) },
                    onDeactivate = { viewModel.deactivate(sensor) },
                    onReactivate = { viewModel.reactivate(sensor) }
                )
            }

            if (data.sensors.isEmpty()) {
                PanelCard { EmptyMessage(strings.t(R.string.noData)) }
            }
        }
    }

    if (viewModel.showFormDialog.value) {
        val dialogTitle = strings.t(if (editingId != null) R.string.editDevice else R.string.addDevice)

        AlertDialog(
            onDismissRequest = viewModel::closeForm,
            title = { Text(dialogTitle) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextInput(
                        label = "${strings.t(R.string.sensorName)} *",
                        value = form.name,
                        onValueChange = { viewModel.onFormChanged(form.copy(name = it)) }
                    )
                    TextInput(
                        label = "${strings.t(R.string.sensorCode)} *",
                        value = form.code,
                        onValueChange = { viewModel.onFormChanged(form.copy(code = it)) }
                    )
                    DropdownField(
                        label = "${strings.t(R.string.sensorType)} *",
                        options = sensorTypes.map { trSensorType(strings, it) to it },
                        selected = form.type,
                        onSelected = { viewModel.onFormChanged(form.copy(type = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownField(
                        label = "${strings.t(R.string.zone)} *",
                        options = data.zones.map { trZone(strings, it.name) to it.id },
                        selected = form.zoneId ?: -1,
                        onSelected = { viewModel.onFormChanged(form.copy(zoneId = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextInput(
                        label = strings.t(R.string.locationDetail),
                        value = form.locationDetail,
                        onValueChange = { viewModel.onFormChanged(form.copy(locationDetail = it)) }
                    )

                    if (formError != null) {
                        FormError(strings.t(formError))
                    }
                }
            },
            dismissButton = {
                SecondaryButton(label = strings.t(R.string.cancelAction), onClick = viewModel::closeForm)
            },
            confirmButton = {
                PrimaryButton(
                    label = strings.t(R.string.saveAction),
                    icon = Icons.Outlined.Check,
                    loading = viewModel.formLoading.value,
                    onClick = viewModel::saveSensor
                )
            }
        )
    }

    if (selectedSensor != null) {
        AlertDialog(
            onDismissRequest = viewModel::closeDetail,
            title = { Text(trSensorName(strings, selectedSensor.name)) },
            text = {
                Column {
                    DetailRow(strings.t(R.string.sensorCode), selectedSensor.code)
                    DetailRow(strings.t(R.string.sensorType), trSensorType(strings, selectedSensor.type))
                    DetailRow(strings.t(R.string.zone), zoneName(selectedSensor.zoneId))
                    DetailRow(strings.t(R.string.locationDetail), trLocationDetail(strings, selectedSensor.locationDetail))
                    DetailRow(strings.t(R.string.status)) {
                        Tag(
                            value = trStatus(strings, selectedSensor.status),
                            severity = statusSeverity(selectedSensor.status)
                        )
                    }
                    DetailRow(strings.t(R.string.battery), "${selectedSensor.battery}%")
                    DetailRow(strings.t(R.string.lastReadingLabel), selectedSensor.lastReading)
                    DetailRow(strings.t(R.string.lastConnection), formatDate(selectedSensor.lastConnected))
                }
            },
            confirmButton = {
                SecondaryButton(label = strings.t(R.string.closeAction), onClick = viewModel::closeDetail)
            }
        )
    }
}

/** Cada fila del DataTable de la web se muestra como una tarjeta en el telefono. */
@Composable
private fun SensorCard(
    strings: Strings,
    sensor: Sensor,
    zoneName: String,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit
) {
    PanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trSensorName(strings, sensor.name),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${sensor.code} · ${trSensorType(strings, sensor.type)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Tag(value = trStatus(strings, sensor.status), severity = statusSeverity(sensor.status))
        }

        Column {
            DetailRow(strings.t(R.string.zone), zoneName)
            DetailRow(strings.t(R.string.battery), "${sensor.battery}%")
            DetailRow(strings.t(R.string.lastReadingLabel), sensor.lastReading)
            DetailRow(strings.t(R.string.lastConnection), formatDate(sensor.lastConnected))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onDetail) {
                Icon(Icons.Outlined.Info, contentDescription = strings.t(R.string.viewDetails))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = strings.t(R.string.editDevice))
            }
            if (sensor.status != "Offline") {
                IconButton(onClick = onDeactivate) {
                    Icon(Icons.Outlined.Block, contentDescription = strings.t(R.string.deactivate), tint = TagWarning)
                }
            } else {
                IconButton(onClick = onReactivate) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = strings.t(R.string.reactivate), tint = TagSuccess)
                }
            }
        }
    }
}
