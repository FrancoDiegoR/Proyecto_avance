package com.smartgas_frontend

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.smartgas_frontend.incidentpreventionnotification.presentation.di.IncidentPreventionNotificationPresentationModule
import com.smartgas_frontend.incidentpreventionnotification.presentation.push.EXTRA_ALERT_ID
import com.smartgas_frontend.incidentpreventionnotification.presentation.push.PUSH_CHECK_MILLIS
import com.smartgas_frontend.shared.i18n.localizedStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.smartgas_frontend.shared.data.di.SharedDataModule
import com.smartgas_frontend.shared.i18n.ProvideStrings
import com.smartgas_frontend.shared.presentation.components.LocalToaster
import com.smartgas_frontend.shared.presentation.components.ToastHost
import com.smartgas_frontend.shared.presentation.components.Toaster
import com.smartgas_frontend.shared.presentation.navigation.AppNavigation
import com.smartgas_frontend.ui.theme.SmartGasTheme

class MainActivity : ComponentActivity() {

    // Id de la alerta a abrir cuando la app se abre desde una notificacion push (diseno 11 -> 12)
    private var pendingAlertId by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = SharedDataModule.getAppPreferences()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readAlertExtra(intent)
        startPushChecks()
        setContent {
            val darkMode = preferences.darkMode
            val language = preferences.language

            LaunchedEffect(darkMode) {
                val transparent = Color.Transparent.toArgb()
                val style = if (darkMode) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            SmartGasTheme(darkTheme = darkMode) {
                ProvideStrings(language = language) {
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()
                    val toaster = remember { Toaster(snackbarHostState, scope) }

                    CompositionLocalProvider(LocalToaster provides toaster) {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AppNavigation(
                                    pendingAlertId = pendingAlertId,
                                    onAlertOpened = { pendingAlertId = null }
                                )
                                ToastHost(
                                    hostState = snackbarHostState,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding()
                                        .imePadding()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // La notificacion se toco con la app ya abierta (launchMode singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readAlertExtra(intent)
    }

    private fun readAlertExtra(intent: Intent?) {
        val id = intent?.getIntExtra(EXTRA_ALERT_ID, -1) ?: -1
        if (id >= 0) pendingAlertId = id
    }

    /**
     * Diseno 11: mientras la app vive (primer plano o segundo plano) revisa cada 10 s si hay
     * alertas nuevas y las muestra como notificacion del sistema.
     */
    private fun startPushChecks() {
        IncidentPreventionNotificationPresentationModule.getAlertNotifier().createChannel()
        val checker = IncidentPreventionNotificationPresentationModule.getPushAlertChecker()
        val preferences = SharedDataModule.getAppPreferences()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                while (true) {
                    try {
                        checker.check(localizedStrings(this@MainActivity, preferences.language))
                    } catch (e: Exception) {
                        // Sin conexion: se vuelve a intentar en la siguiente vuelta
                    }
                    delay(PUSH_CHECK_MILLIS)
                }
            }
        }
    }
}
