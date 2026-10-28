package com.smartgas_frontend.iam.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartgas_frontend.R
import com.smartgas_frontend.shared.data.di.SharedDataModule
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Orange

// Componentes visuales compartidos por Login y Create account (disenos 01-03).

/** Logo de texto del diseno: SMART en azul y GAS en naranja. */
@Composable
fun SmartGasWordmark(modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Blue)) { append("SMART") }
            withStyle(SpanStyle(color = Orange)) { append("GAS") }
        },
        fontSize = 40.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = modifier.semantics { contentDescription = "SmartGas" }
    )
}

/** Campo de texto del diseno (outlined, sin icono). Si [password] es true muestra el "ojo" para ver/ocultar. */
@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    isError: Boolean = false
) {
    val strings = LocalStrings.current
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        trailingIcon = if (password) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = strings.t(if (visible) R.string.hidePassword else R.string.showPassword)
                    )
                }
            }
        } else {
            null
        },
        shape = RoundedCornerShape(10.dp)
    )
}

/** Boton principal con forma de pildora ("Sign in", "Continue"). */
@Composable
fun AuthPrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Boton secundario con borde ("Create an account"). */
@Composable
fun AuthOutlinedButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Blue)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Blue)
    }
}

/** Checkbox con texto; toda la fila es tocable. */
@Composable
fun CheckLine(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * "I accept the Terms and Conditions and the Privacy Policy." con los nombres resaltados en azul.
 * Aun no hay paginas de terminos, por eso no navegan a ningun lado.
 */
@Composable
fun TermsCheckLine(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val terms = strings.t(R.string.termsAndConditions)
    val privacy = strings.t(R.string.privacyPolicy)
    val template = strings.t(R.string.termsAccept)

    val text = buildAnnotatedString {
        var rest = template
        while (rest.isNotEmpty()) {
            val termsAt = rest.indexOf("{terms}")
            val privacyAt = rest.indexOf("{privacy}")
            val next = listOf(termsAt, privacyAt).filter { it >= 0 }.minOrNull()

            if (next == null) {
                append(rest)
                rest = ""
            } else {
                append(rest.substring(0, next))
                val isTerms = next == termsAt
                withStyle(SpanStyle(color = Blue, fontWeight = FontWeight.SemiBold)) {
                    append(if (isTerms) terms else privacy)
                }
                rest = rest.substring(next + if (isTerms) "{terms}".length else "{privacy}".length)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Pie del diseno: "🌐 English (US) · Español (LatAm)". El idioma activo se ve resaltado. */
@Composable
fun LanguageFooter(modifier: Modifier = Modifier) {
    val preferences = SharedDataModule.getAppPreferences()
    val locale = preferences.language

    Row(
        modifier = modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Outlined.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        LanguageOption("English (US)", active = locale == "en") { preferences.setSmartGasLocale("en") }
        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LanguageOption("Español (LatAm)", active = locale == "es") { preferences.setSmartGasLocale("es") }
    }
}

@Composable
private fun LanguageOption(label: String, active: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp)
    )
}
