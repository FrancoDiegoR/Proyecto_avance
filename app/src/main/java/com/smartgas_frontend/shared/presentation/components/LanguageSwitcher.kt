package com.smartgas_frontend.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.shared.data.di.SharedDataModule

@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    val preferences = SharedDataModule.getAppPreferences()
    val locale = preferences.language

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .semantics { contentDescription = "Language selector" }
    ) {
        LangButton(text = "EN", description = "Switch to English", active = locale == "en") {
            preferences.setSmartGasLocale("en")
        }
        LangButton(text = "ES", description = "Cambiar a Español", active = locale == "es") {
            preferences.setSmartGasLocale("es")
        }
    }
}

@Composable
private fun LangButton(text: String, description: String, active: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodySmall,
        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                selected = active
            }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}
