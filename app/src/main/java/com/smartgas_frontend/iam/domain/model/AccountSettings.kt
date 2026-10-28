package com.smartgas_frontend.iam.domain.model

data class AccountSettings(
    val safety: SafetySettings = SafetySettings(),
    val notifications: NotificationPreferences = NotificationPreferences(),
    val language: String = "en",
    val darkMode: Boolean = false
)
