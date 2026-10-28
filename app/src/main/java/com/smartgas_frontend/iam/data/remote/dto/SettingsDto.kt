package com.smartgas_frontend.iam.data.remote.dto

import com.smartgas_frontend.iam.domain.model.AccountSettings
import com.smartgas_frontend.iam.domain.model.NotificationPreferences
import com.smartgas_frontend.iam.domain.model.SafetySettings
import com.smartgas_frontend.shared.data.local.normalizeLocale

data class SettingsDto(
    val language: String?,
    val darkMode: Boolean?,
    val notificationsEnabled: Boolean?,
    val gasThreshold: Double?,
    val temperatureThreshold: Double?
)

data class UpdateSettingsRequest(
    val language: String,
    val darkMode: Boolean,
    val notificationsEnabled: Boolean,
    val gasThreshold: Double,
    val temperatureThreshold: Double
)

fun toApiLanguage(language: String): String =
    if (language == "es" || language == "es-419") "es-419" else "en-US"

fun SettingsDto.toAccountSettings() = AccountSettings(
    safety = SafetySettings(
        gasThreshold = gasThreshold ?: 50.0,
        temperatureThreshold = temperatureThreshold ?: 45.0
    ),
    notifications = NotificationPreferences(notificationsEnabled = notificationsEnabled == true),
    language = normalizeLocale(language),
    darkMode = darkMode == true
)

fun AccountSettings.toRequest() = UpdateSettingsRequest(
    language = toApiLanguage(language),
    darkMode = darkMode,
    notificationsEnabled = notifications.notificationsEnabled,
    gasThreshold = safety.gasThreshold,
    temperatureThreshold = safety.temperatureThreshold
)
