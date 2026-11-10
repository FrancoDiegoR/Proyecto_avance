package com.smartgas_frontend.iam.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ContactEmergency
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.iam.domain.model.EmergencyContact
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.TextInput
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.presentation.navigation.Routes
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.trPlanName
import com.smartgas_frontend.ui.theme.Red

// Rangos de los sliders del diseno 15
private const val GAS_MIN = 10f
private const val GAS_MAX = 100f
private const val TEMP_MIN = 30f
private const val TEMP_MAX = 80f

/** Idiomas que ofrece la app (codigo -> nombre como en el pie del login). */
private val languages = listOf("en" to "English (US)", "es" to "Español (LatAm)")

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(es: Boolean) {
    val title = if (es) "Ajustes" else "Settings"
    val thresholds = if (es) "Umbrales de seguridad" else "Safety thresholds"
    val gasAlert = if (es) "Alerta de gas en" else "Gas alert at"
    val tempAlert = if (es) "Alerta de temperatura en" else "Temperature alert at"
    val criticalRule =
        if (es) "Crítico cuando el gas ≥ 1.5× o la temperatura ≥ 1.3× el umbral."
        else "Critical when gas ≥ 1.5× or temperature ≥ 1.3× the threshold."
    val push = if (es) "Notificaciones push" else "Push notifications"
    val vibrate = if (es) "Vibrar en alertas críticas" else "Vibrate on critical alerts"
    val darkMode = if (es) "Modo oscuro" else "Dark mode"
    val emergencyContact = if (es) "Contacto de emergencia" else "Emergency contact"
    val noContact = if (es) "Sin configurar" else "Not set"
    val language = if (es) "Idioma" else "Language"
    val plan = "Plan"
    val reports = if (es) "Reportes" else "Reports"
    val signOut = if (es) "Cerrar sesión" else "Sign out"
    val name = if (es) "Nombre" else "Name"
    val phone = if (es) "Teléfono" else "Phone"
    val email = if (es) "Correo" else "Email"
    val save = if (es) "Guardar" else "Save"
    val cancel = if (es) "Cancelar" else "Cancel"
    val loadError = if (es) "No se pudieron cargar los ajustes." else "Settings could not be loaded."
}

/** Diseno 15: Settings. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val state = viewModel.state.value
    val data = state.data ?: SettingsData()
    val preferences = viewModel.preferences

    var editingContact by remember { mutableStateOf(false) }
    var choosingLanguage by remember { mutableStateOf(false) }

    ToastEffect(viewModel.toasts)

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    ContentPage {
        Text(
            pageText.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )

        if (state.message.isNotEmpty()) {
            AlertBanner(pageText.loadError)
        }

        // Tarjeta "Safety thresholds" con dos sliders
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(pageText.thresholds, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                ThresholdSlider(
                    label = pageText.gasAlert,
                    valueLabel = "${formatNumber(data.gasThreshold)} ppm",
                    value = data.gasThreshold.toFloat(),
                    range = GAS_MIN..GAS_MAX,
                    onChange = viewModel::onGasThresholdChanged,
                    onDone = viewModel::saveThresholds
                )
                ThresholdSlider(
                    label = pageText.tempAlert,
                    valueLabel = "${formatNumber(data.temperatureThreshold)} °C",
                    value = data.temperatureThreshold.toFloat(),
                    range = TEMP_MIN..TEMP_MAX,
                    onChange = viewModel::onTemperatureThresholdChanged,
                    onDone = viewModel::saveThresholds
                )
                Text(pageText.criticalRule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Tarjeta con interruptores y opciones
        SettingsCard {
            SwitchItem(Icons.Outlined.NotificationsActive, pageText.push, preferences.pushEnabled, viewModel::setPushEnabled)
            Divider()
            SwitchItem(Icons.Outlined.Vibration, pageText.vibrate, preferences.vibrateOnCritical, viewModel::setVibrateOnCritical)
            Divider()
            SwitchItem(Icons.Outlined.DarkMode, pageText.darkMode, preferences.darkMode, viewModel::setDarkMode)
            Divider()
            NavItem(
                icon = Icons.Outlined.ContactEmergency,
                title = pageText.emergencyContact,
                subtitle = if (data.contact.name.isBlank()) pageText.noContact else "${data.contact.name} · ${data.contact.phone}",
                onClick = { editingContact = true }
            )
            Divider()
            NavItem(
                icon = Icons.Outlined.Translate,
                title = pageText.language,
                value = languages.firstOrNull { it.first == preferences.language }?.second,
                onClick = { choosingLanguage = true }
            )
            Divider()
            NavItem(
                icon = Icons.Outlined.WorkspacePremium,
                title = pageText.plan,
                value = data.planName.ifBlank { null }?.let { trPlanName(strings, it) },
                onClick = { onNavigate(Routes.PLANS) }
            )
            Divider()
            NavItem(icon = Icons.Outlined.Description, title = pageText.reports, onClick = { onNavigate(Routes.REPORTS) })
            Divider()
            // Fila roja "Sign out"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Red)
                Text(pageText.signOut, color = Red, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (editingContact) {
        ContactDialog(
            pageText = pageText,
            initial = data.contact,
            onDismiss = { editingContact = false },
            onSave = { contact -> viewModel.saveEmergencyContact(contact) { editingContact = false } }
        )
    }

    if (choosingLanguage) {
        LanguageDialog(
            pageText = pageText,
            selected = preferences.language,
            onDismiss = { choosingLanguage = false },
            onSelect = { code ->
                choosingLanguage = false
                viewModel.setLanguage(code)
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
}

/** "Gas alert at ........ 50 ppm" + slider. Se guarda al soltar (onDone) para no llamar a la API en cada paso. */
@Composable
private fun ThresholdSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    onDone: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            onValueChangeFinished = onDone,
            valueRange = range
        )
    }
}

@Composable
private fun SwitchItem(icon: ImageVector, title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** Fila con flecha ">" (contacto, idioma, plan...). [value] se muestra a la derecha. */
@Composable
private fun NavItem(icon: ImageVector, title: String, onClick: () -> Unit, subtitle: String? = null, value: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ContactDialog(
    pageText: PageText,
    initial: EmergencyContact,
    onDismiss: () -> Unit,
    onSave: (EmergencyContact) -> Unit
) {
    var contact by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pageText.emergencyContact) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextInput(label = pageText.name, value = contact.name, onValueChange = { contact = contact.copy(name = it) })
                TextInput(
                    label = pageText.phone,
                    value = contact.phone,
                    onValueChange = { contact = contact.copy(phone = it) },
                    keyboardType = KeyboardType.Phone
                )
                TextInput(
                    label = pageText.email,
                    value = contact.email,
                    onValueChange = { contact = contact.copy(email = it) },
                    keyboardType = KeyboardType.Email
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(contact) }) { Text(pageText.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(pageText.cancel) } }
    )
}

@Composable
private fun LanguageDialog(pageText: PageText, selected: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pageText.language) },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = code == selected, role = Role.RadioButton, onClick = { onSelect(code) })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = code == selected, onClick = null)
                        Text(name, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(pageText.cancel) } }
    )
}
