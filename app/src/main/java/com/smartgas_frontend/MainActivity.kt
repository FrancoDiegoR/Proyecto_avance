package com.smartgas_frontend

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = SharedDataModule.getAppPreferences()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                AppNavigation()
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
}
