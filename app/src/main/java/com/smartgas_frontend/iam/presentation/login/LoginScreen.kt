package com.smartgas_frontend.iam.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.iam.presentation.components.AuthOutlinedButton
import com.smartgas_frontend.iam.presentation.components.AuthPrimaryButton
import com.smartgas_frontend.iam.presentation.components.AuthTextField
import com.smartgas_frontend.iam.presentation.components.CheckLine
import com.smartgas_frontend.iam.presentation.components.LanguageFooter
import com.smartgas_frontend.iam.presentation.components.SmartGasWordmark
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.FormError
import com.smartgas_frontend.ui.theme.Blue

/** Disenos 01 (Sign in) y 02 (Sign in error). */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit
) {
    val strings = LocalStrings.current
    val email = viewModel.email.value
    val password = viewModel.password.value
    val remember = viewModel.remember.value
    val error = viewModel.error.value
    val loading = viewModel.loading.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        // El formulario hace scroll; el selector de idioma queda fijo abajo
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(64.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                SmartGasWordmark()
                Text(
                    strings.t(R.string.authTagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                strings.t(R.string.signInTitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            AuthTextField(
                label = strings.t(R.string.email),
                value = email,
                onValueChange = viewModel::onEmailChanged,
                keyboardType = KeyboardType.Email,
                isError = error != null
            )

            AuthTextField(
                label = strings.t(R.string.password),
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                password = true,
                isError = error != null
            )

            // Diseno 02: el error aparece debajo de la contrasena
            if (error != null) {
                FormError(strings.t(error))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckLine(
                    text = strings.t(R.string.keepSignedIn),
                    checked = remember,
                    onCheckedChange = viewModel::onRememberChanged,
                    modifier = Modifier.weight(1f)
                )
                // El backend aun no tiene recuperacion de contrasena: el enlace se muestra pero no hace nada
                TextButton(onClick = {}) {
                    Text(strings.t(R.string.forgotPassword), color = Blue, fontWeight = FontWeight.SemiBold)
                }
            }

            AuthPrimaryButton(
                label = strings.t(R.string.signInTitle),
                loading = loading,
                onClick = { viewModel.login(onLoggedIn) }
            )

            AuthOutlinedButton(
                label = strings.t(R.string.createAnAccount),
                onClick = onRegister
            )
        }

        LanguageFooter(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
