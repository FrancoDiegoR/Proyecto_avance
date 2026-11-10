package com.smartgas_frontend.shared.data.local

import android.content.Context
import com.google.gson.Gson
import com.smartgas_frontend.shared.domain.model.HomeSummary
import com.smartgas_frontend.shared.domain.model.Weather
import com.smartgas_frontend.shared.utils.nowISO

private const val FILE = "smartgas_home_cache"
private const val SUMMARY_KEY = "summary"
private const val WEATHER_KEY = "weather"
private const val SAVED_AT_KEY = "saved_at"

/**
 * Diseno 17 (modo offline): guarda el ultimo resumen del Home que llego bien del backend.
 * Si luego no hay conexion, HomeRepository devuelve esta copia y la pantalla muestra
 * "You are offline · showing data saved at 10:31".
 * Se usa SharedPreferences + Gson (igual que SessionService) porque es un unico objeto pequeno.
 */
class HomeCache(context: Context) {
    private val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSummary(summary: HomeSummary) {
        preferences.edit()
            .putString(SUMMARY_KEY, gson.toJson(summary.copy(offlineSavedAt = null)))
            .putString(SAVED_AT_KEY, nowISO())
            .apply()
    }

    /** Ultimo resumen guardado, marcado con la hora en que se guardo; null si nunca hubo uno. */
    fun loadSummary(): HomeSummary? = try {
        preferences.getString(SUMMARY_KEY, null)
            ?.let { gson.fromJson(it, HomeSummary::class.java) }
            ?.copy(offlineSavedAt = preferences.getString(SAVED_AT_KEY, null))
    } catch (e: Exception) {
        null
    }

    fun saveWeather(weather: Weather) {
        preferences.edit().putString(WEATHER_KEY, gson.toJson(weather)).apply()
    }

    fun loadWeather(): Weather? = try {
        preferences.getString(WEATHER_KEY, null)?.let { gson.fromJson(it, Weather::class.java) }
    } catch (e: Exception) {
        null
    }

    /** Al cerrar sesion no deben quedar datos de la cuenta anterior. */
    fun clear() {
        preferences.edit().clear().apply()
    }
}
