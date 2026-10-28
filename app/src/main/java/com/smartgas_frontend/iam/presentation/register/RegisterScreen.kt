package com.smartgas_frontend.iam.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.iam.presentation.components.AuthPrimaryButton
import com.smartgas_frontend.iam.presentation.components.AuthTextField
import com.smartgas_frontend.iam.presentation.components.TermsCheckLine
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.components.DropdownField
import com.smartgas_frontend.shared.presentation.components.FormError

/** Diseno 03: Create account. */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onGoToDashboard: () -> Unit,
    onLogin: () -> Unit
) {
    val strings = LocalStrings.current
    val form = viewModel.form.value
    val error = viewModel.error.value
    val loading = viewModel.loading.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Flecha atras + titulo
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLogin) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.t(R.string.signInTitle))
                }
                Text(
                    strings.t(R.string.createAccountTitle),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
            }

            Text(
                strings.t(R.string.accountDetails),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Chips Home / Restaurant
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountTypeChip(
                    label = strings.t(R.string.accountHome),
                    icon = Icons.Outlined.Home,
                    selected = !form.isRestaurant,
                    onClick = { viewModel.onFormChanged(form.copy(accountType = ACCOUNT_HOME)) }
                )
                AccountTypeChip(
                    label = strings.t(R.string.accountRestaurant),
                    icon = Icons.Outlined.Restaurant,
                    selected = form.isRestaurant,
                    onClick = { viewModel.onFormChanged(form.copy(accountType = ACCOUNT_RESTAURANT)) }
                )
            }

            AuthTextField(
                label = strings.t(R.string.fullName),
                value = form.fullName,
                onValueChange = { viewModel.onFormChanged(form.copy(fullName = it)) }
            )
            AuthTextField(
                label = strings.t(R.string.businessName),
                value = form.businessName,
                onValueChange = { viewModel.onFormChanged(form.copy(businessName = it)) }
            )
            AuthTextField(
                label = strings.t(R.string.email),
                value = form.email,
                onValueChange = { viewModel.onFormChanged(form.copy(email = it)) },
                keyboardType = KeyboardType.Email
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuthTextField(
                    label = strings.t(R.string.password),
                    value = form.password,
                    onValueChange = { viewModel.onFormChanged(form.copy(password = it)) },
                    password = true
                )
                Text(
                    strings.t(R.string.passwordRule),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownField(
                label = strings.t(R.string.district),
                options = limaDistricts.map { it to it },
                selected = form.district,
                onSelected = { viewModel.onFormChanged(form.copy(district = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            TermsCheckLine(
                checked = form.acceptedTerms,
                onCheckedChange = { viewModel.onFormChanged(form.copy(acceptedTerms = it)) }
            )

            if (error != null) {
                FormError(strings.t(error))
            }
        }

        // Boton fijo abajo, como en el diseno
        AuthPrimaryButton(
            label = strings.t(R.string.continueAction),
            loading = loading,
            onClick = { viewModel.register(onGoToDashboard) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun AccountTypeChip(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
    )
}
