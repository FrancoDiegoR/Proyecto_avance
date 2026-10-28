package com.smartgas_frontend.shared.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val FILE = "smartgas_preferences"
private const val THEME_KEY = "smartgas-theme"
private const val LANGUAGE_KEY = "smartgas-language"
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
