package com.smartgas_frontend.shared.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.ui.theme.TagDanger
import com.smartgas_frontend.ui.theme.TagInfo
import com.smartgas_frontend.ui.theme.TagSuccess
import com.smartgas_frontend.ui.theme.TagWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

enum class ToastSeverity { Success, Info, Warning, Error }

/** Texto que se resuelve en la pantalla con el idioma activo (equivale a llamar `t()` en la vista). */
typealias UiText = (Strings) -> String

fun res(@StringRes id: Int, vararg params: Pair<String, Any?>): UiText = { it.t(id, *params) }

data class ToastMessage(
    val severity: ToastSeverity,
    val summary: UiText,
    val detail: UiText? = null
)

private class ToastVisuals(
    val severity: ToastSeverity,
    override val message: String,
    val detail: String?
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

/** Equivalente al ToastService de PrimeVue. */
class Toaster(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    fun add(severity: ToastSeverity, summary: String, detail: String? = null) {
        scope.launch {
            hostState.showSnackbar(ToastVisuals(severity, summary, detail))
        }
    }
}

val LocalToaster = staticCompositionLocalOf<Toaster> {
    error("Toaster not provided")
}

@Composable
fun ToastHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        val visuals = data.visuals as? ToastVisuals
        val container = when (visuals?.severity) {
            ToastSeverity.Success -> TagSuccess
            ToastSeverity.Warning -> TagWarning
            ToastSeverity.Error -> TagDanger
            else -> TagInfo
        }

        Snackbar(
            modifier = Modifier.padding(12.dp),
            containerColor = container,
            contentColor = Color.White
        ) {
            Column {
                Text(data.visuals.message, fontWeight = FontWeight.SemiBold)
                visuals?.detail?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

/** ViewModel base que emite toasts para que la pantalla los muestre traducidos. */
abstract class ToastViewModel : ViewModel() {
    private val _toasts = MutableSharedFlow<ToastMessage>(extraBufferCapacity = 8)
    val toasts: SharedFlow<ToastMessage> get() = _toasts

    protected fun toast(severity: ToastSeverity, summary: UiText, detail: UiText? = null) {
        _toasts.tryEmit(ToastMessage(severity, summary, detail))
    }
}

@Composable
fun ToastEffect(toasts: Flow<ToastMessage>) {
    val toaster = LocalToaster.current
    val strings = LocalStrings.current

    LaunchedEffect(toasts, strings) {
        toasts.collect { message ->
            toaster.add(message.severity, message.summary(strings), message.detail?.invoke(strings))
        }
    }
}
