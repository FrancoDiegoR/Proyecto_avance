package com.smartgas_frontend.iam.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DropdownField
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.IconBadge
import com.smartgas_frontend.shared.presentation.components.ListRow
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.SwitchRow
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.TextInput
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.presentation.navigation.Routes
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.trSensorName
import com.smartgas_frontend.shared.utils.trSensorType
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.TagDanger

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val data = state.data ?: SettingsData()
    val loading = state.isLoading
    val form = viewModel.form.value
    val emergencyContact = viewModel.emergencyContact.value

    ToastEffect(viewModel.toasts)

    ContentPage {
        PageHeader(title = strings.t(R.string.settingsTitle), subtitle = strings.t(R.string.settingsSubtitle)) {
            PrimaryButton(
                label = strings.t(R.string.saveAction),
                icon = Icons.Outlined.Check,
                loading = viewModel.saveLoading.value,
                onClick = viewModel::saveAll
            )
            SecondaryButton(
                label = strings.t(R.string.resetAction),
                icon = Icons.AutoMirrored.Outlined.Undo,
                onClick = viewModel::resetAll
            )
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (loading) {
            LoadingState()
        }

        if (!loading) {
            SettingsCard(
                icon = Icons.Outlined.Shield,
                title = strings.t(R.string.safetyThresholds),
                subtitle = strings.t(R.string.thresholdsHelp)
            ) {
                TextInput(
                    label = strings.t(R.string.gasWarningLimit),
                    value = form.gasThreshold,
                    onValueChange = { viewModel.onFormChanged(form.copy(gasThreshold = it)) },
                    keyboardType = KeyboardType.Number
                )
                TextInput(
                    label = strings.t(R.string.temperatureWarningLimit),
                    value = form.temperatureThreshold,
                    onValueChange = { viewModel.onFormChanged(form.copy(temperatureThreshold = it)) },
                    keyboardType = KeyboardType.Number
                )
            }

            SettingsCard(
                icon = Icons.Outlined.LocationOn,
                title = strings.t(R.string.zoneConfiguration),
                subtitle = strings.t(R.string.monitoredZones)
            ) {
                data.zones.forEach { zone ->
                    ListRow {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trZone(strings, zone.name), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "${strings.t(R.string.sensorCountText, "count" to data.sensorCount(zone.id))} · ${zone.sensitivity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Tag(
                            value = strings.t(if (zone.status == "Safe") R.string.activeZone else R.string.attentionZone),
                            severity = when (zone.status) {
                                "Safe" -> TagSeverity.Success
                                "Critical" -> TagSeverity.Danger
                                else -> TagSeverity.Warning
                            }
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryButton(label = strings.t(R.string.openMonitoring), onClick = { onNavigate(Routes.ZONES) })
                    SecondaryButton(label = strings.t(R.string.manageDevices), onClick = { onNavigate(Routes.SENSORS) })
                }
            }

            SettingsCard(
                icon = Icons.Outlined.Phone,
                title = strings.t(R.string.emergencyContact),
                subtitle = strings.t(R.string.notifyEmergencyContact)
            ) {
                TextInput(
                    label = strings.t(R.string.emergencyName),
                    value = emergencyContact.name,
                    onValueChange = { viewModel.onEmergencyContactChanged(emergencyContact.copy(name = it)) }
                )
                TextInput(
                    label = strings.t(R.string.emergencyPhone),
                    value = emergencyContact.phone,
                    onValueChange = { viewModel.onEmergencyContactChanged(emergencyContact.copy(phone = it)) },
                    keyboardType = KeyboardType.Phone
                )
                TextInput(
                    label = strings.t(R.string.emergencyEmail),
                    value = emergencyContact.email,
                    onValueChange = { viewModel.onEmergencyContactChanged(emergencyContact.copy(email = it)) },
                    keyboardType = KeyboardType.Email
                )
            }

            SettingsCard(
                icon = Icons.Outlined.Notifications,
                title = strings.t(R.string.notificationPreferences),
                subtitle = strings.t(R.string.notificationChannels)
            ) {
                SwitchRow(
                    label = strings.t(R.string.notificationPreferences),
                    checked = form.notificationsEnabled,
                    onCheckedChange = { viewModel.onFormChanged(form.copy(notificationsEnabled = it)) }
                )
                Text(
                    strings.t(R.string.latestNotifications),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingsCard(
                icon = Icons.Outlined.Memory,
                title = strings.t(R.string.connectedSensors),
                subtitle = strings.t(R.string.sensorPreferencesHelp)
            ) {
                if (data.sensors.isEmpty()) {
                    EmptyMessage(strings.t(R.string.noData))
                }

                data.sensors.forEach { sensor ->
                    ListRow {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trSensorName(strings, sensor.name), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "${sensor.code} · ${trSensorType(strings, sensor.type)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Tag(
                            value = trStatus(strings, sensor.status),
                            severity = when (sensor.status) {
                                "Online" -> TagSeverity.Success
                                "Critical" -> TagSeverity.Danger
                                else -> TagSeverity.Warning
                            }
                        )
                    }
                }
            }

            SettingsCard(
                icon = Icons.Outlined.Language,
                title = strings.t(R.string.interfacePreferences),
                subtitle = strings.t(R.string.languagePreference)
            ) {
                DropdownField(
                    label = strings.t(R.string.languagePreference),
                    options = listOf("English" to "en", "Español" to "es"),
                    selected = form.language,
                    onSelected = { viewModel.onFormChanged(form.copy(language = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                SwitchRow(
                    label = strings.t(R.string.darkMode),
                    checked = form.darkMode,
                    onCheckedChange = { viewModel.onFormChanged(form.copy(darkMode = it)) }
                )
            }
        }

        // "Sign out" del diseno (15 Settings). Fuera del if(!loading) para que siempre se vea.
        PrimaryButton(
            label = strings.t(R.string.logout),
            icon = Icons.AutoMirrored.Outlined.Logout,
            onClick = onLogout,
            containerColor = TagDanger,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** .settings-card */
@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(icon)
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}
