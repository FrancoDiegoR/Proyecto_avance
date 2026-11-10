package com.smartgas_frontend.shared.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val FILE = "smartgas_preferences"
private const val THEME_KEY = "smartgas-theme"
private const val LANGUAGE_KEY = "smartgas-language"
private const val PUSH_KEY = "smartgas-push"
private const val VIBRATE_KEY = "smartgas-vibrate-critical"
private const val DEFAULT_LOCALE = "en"
private val SUPPORTED_LOCALES = listOf("en", "es")

fun normalizeLocale(locale: String?): String =
    if (locale == "es" || locale == "es-419") "es" else "en"

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var language by mutableStateOf(readLanguage())
        private set

    var darkMode by mutableStateOf(preferences.getString(THEME_KEY, "light") == "dark")
        private set

    // Diseno 15: interruptores "Push notifications" y "Vibrate on critical alerts".
    // Se guardan en el telefono porque los usa el aviso push (diseno 11) sin consultar al backend.
    var pushEnabled by mutableStateOf(preferences.getBoolean(PUSH_KEY, true))
        private set

    var vibrateOnCritical by mutableStateOf(preferences.getBoolean(VIBRATE_KEY, true))
        private set

    fun updatePushEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(PUSH_KEY, enabled).apply()
        pushEnabled = enabled
    }

    fun updateVibrateOnCritical(enabled: Boolean) {
        preferences.edit().putBoolean(VIBRATE_KEY, enabled).apply()
        vibrateOnCritical = enabled
    }

    private fun readLanguage(): String {
        val saved = preferences.getString(LANGUAGE_KEY, null)
        return if (saved != null && saved in SUPPORTED_LOCALES) saved else DEFAULT_LOCALE
    }

    fun setSmartGasLocale(locale: String): String {
        val normalized = normalizeLocale(locale)
        preferences.edit().putString(LANGUAGE_KEY, normalized).apply()
        language = normalized
        return normalized
    }

    fun setDarkTheme(enabled: Boolean) {
        preferences.edit().putString(THEME_KEY, if (enabled) "dark" else "light").apply()
        darkMode = enabled
    }
}
