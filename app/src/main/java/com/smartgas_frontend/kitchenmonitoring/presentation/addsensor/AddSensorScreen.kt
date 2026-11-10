package com.smartgas_frontend.kitchenmonitoring.presentation.addsensor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.smartgas_frontend.kitchenmonitoring.presentation.components.startQrScan
import com.smartgas_frontend.ui.theme.Blue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.kitchenmonitoring.presentation.sensors.sensorTypes
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DropdownField
import com.smartgas_frontend.shared.presentation.components.FormError
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.LocalToaster
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.TextInput
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.utils.trPlanName
import com.smartgas_frontend.shared.utils.trSensorType
import com.smartgas_frontend.shared.utils.trZone

/** Textos propios de la pagina (mismo patron PageText que el resto de pantallas). */
private class PageText(private val es: Boolean) {
    val title = if (es) "Agregar sensor" else "Add sensor"
    val editTitle = if (es) "Editar sensor" else "Edit sensor"
    val close = if (es) "Cerrar" else "Close"
    val save = if (es) "Guardar sensor" else "Save sensor"
    val scanTitle = if (es) "Escanear el QR del sensor" else "Scan the sensor QR code"
    val scanSubtitle = if (es) "Completa el código y el tipo automáticamente" else "Fills the code and type automatically"
    val orManual = if (es) "— o ingresa los datos manualmente —" else "— or enter the details manually —"
    val scanned = if (es) "QR leído: revisa los datos y guarda." else "QR read: check the details and save."
    val invalidQr = if (es) "Ese QR no es de un sensor SmartGas." else "That QR is not a SmartGas sensor."
    val scanError =
        if (es) "No se pudo abrir el escáner. Ingresa los datos manualmente."
        else "The scanner could not be opened. Enter the details manually."

    fun planInfo(plan: String, max: String, inUse: Int): String =
        if (es) "Tu plan $plan permite $max sensores · $inUse en uso"
        else "Your $plan plan allows $max sensors · $inUse in use"
}

@Composable
fun AddSensorScreen(viewModel: AddSensorViewModel, onClose: () -> Unit, startWithScan: Boolean = false) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val toaster = LocalToaster.current
    val state = viewModel.state.value
    val data = state.data ?: AddSensorData()
    val form = viewModel.form.value
    val formError = viewModel.formError.value
    val context = LocalContext.current

    // Abre el escaner de ML Kit y rellena el formulario con el resultado
    val scan: () -> Unit = {
        startQrScan(
            context = context,
            onResult = { raw ->
                if (viewModel.applyScan(raw)) {
                    toaster.add(ToastSeverity.Success, pageText.scanned)
                } else {
                    toaster.add(ToastSeverity.Warning, pageText.invalidQr)
                }
            },
            onError = { toaster.add(ToastSeverity.Error, pageText.scanError) }
        )
    }

    // Desde el icono QR del Home el escaner se abre solo, una unica vez
    var autoScanDone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(startWithScan) {
        if (startWithScan && !autoScanDone) {
            autoScanDone = true
            scan()
        }
    }

    ContentPage {
        // Barra superior del diseno 08: X para cerrar + titulo
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = pageText.close)
            }
            Text(
                if (viewModel.isEditing) pageText.editTitle else pageText.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )
        }

        if (state.isLoading && state.data == null) {
            LoadingState()
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (state.data != null) {
            // Diseno 08: tarjeta "Scan the sensor QR code" (solo al agregar)
            if (!viewModel.isEditing) {
                ScanQrCard(pageText = pageText, onClick = scan)
                Text(
                    pageText.orManual,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TextInput(
                label = "${strings.t(R.string.sensorCode)} *",
                value = form.code,
                onValueChange = { viewModel.onFormChanged(form.copy(code = it)) },
                modifier = Modifier.fillMaxWidth()
            )
            TextInput(
                label = "${strings.t(R.string.sensorName)} *",
                value = form.name,
                onValueChange = { viewModel.onFormChanged(form.copy(name = it)) },
                modifier = Modifier.fillMaxWidth()
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

            // "Your Professional plan allows 12 sensors · 9 in use" (solo al agregar)
            if (!viewModel.isEditing) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    pageText.planInfo(
                        plan = trPlanName(strings, data.currentPlan),
                        max = if (data.unlimitedSensors) strings.t(R.string.unlimitedShort) else data.maxSensors.toString(),
                        inUse = data.sensors.size
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (formError != null) {
                FormError(strings.t(formError))
            }

            PrimaryButton(
                label = pageText.save,
                icon = Icons.Outlined.Check,
                loading = viewModel.saving.value,
                onClick = {
                    viewModel.saveSensor {
                        // El toast se lanza desde aqui (Toaster de la Activity) porque la pantalla
                        // se cierra enseguida y un toast del ViewModel se perderia.
                        toaster.add(ToastSeverity.Success, strings.t(R.string.deviceSaved))
                        onClose()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Tarjeta azul del diseno 08 que abre el escaner QR (diseno 09). */
@Composable
private fun ScanQrCard(pageText: PageText, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, Blue),
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(pageText.scanTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(pageText.scanSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
