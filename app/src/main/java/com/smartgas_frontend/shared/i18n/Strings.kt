package com.smartgas_frontend.shared.i18n

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Equivalente a `t()` de vue-i18n: resuelve los textos de values/strings.xml (en)
 * y values-es/strings.xml (es) segun el idioma elegido dentro de la app.
 */
class Strings(
    private val resources: Resources,
    private val packageName: String,
    val locale: String
) {
    val isSpanish: Boolean get() = locale == "es"

    fun t(@StringRes id: Int, vararg params: Pair<String, Any?>): String =
        interpolate(resources.getString(id), params)

    fun t(key: String, vararg params: Pair<String, Any?>): String {
        val id = resources.getIdentifier(key, "string", packageName)
        return if (id == 0) key else t(id, *params)
    }

    private fun interpolate(text: String, params: Array<out Pair<String, Any?>>): String =
        params.fold(text) { acc, (name, value) -> acc.replace("{$name}", value?.toString().orEmpty()) }
}

val LocalStrings = staticCompositionLocalOf<Strings> {
    error("Strings not provided")
}

fun localizedStrings(context: Context, language: String): Strings {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(Locale.forLanguageTag(language))
    val localized = context.createConfigurationContext(configuration)
    return Strings(localized.resources, context.packageName, language)
}

@Composable
fun ProvideStrings(language: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val strings = remember(language) { localizedStrings(context, language) }
    CompositionLocalProvider(LocalStrings provides strings, content = content)
}
