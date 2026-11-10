package com.smartgas_frontend.incidentpreventionnotification.presentation.push

import android.content.Context
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.iam.data.repository.SettingsRepository
import com.smartgas_frontend.incidentdetection.data.repository.IncidentRepository
import com.smartgas_frontend.incidentpreventionnotification.data.repository.AlertRepository
import com.smartgas_frontend.incidentpreventionnotification.presentation.components.incidentValueWithLimit
import com.smartgas_frontend.shared.data.local.AppPreferences
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.shared.i18n.Strings

private const val FILE = "smartgas_push"
private const val SEEN_KEY = "seen_ids"
private const val BASELINE_KEY = "baseline_done"

/** Cada cuanto se revisa si hay alertas nuevas mientras la app esta abierta o en segundo plano. */
const val PUSH_CHECK_MILLIS = 10_000L

/**
 * Revisa el backend y avisa con una notificacion del sistema por cada alerta nueva sin confirmar.
 * El proyecto no tiene Firebase (FCM), asi que el "push" se hace consultando la API cada pocos segundos.
 * La primera vez solo memoriza las alertas existentes para no avisar de alertas viejas.
 */
class PushAlertChecker(
    context: Context,
    private val alertRepository: AlertRepository,
    private val incidentRepository: IncidentRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionService: SessionService,
    private val preferences: AppPreferences,
    private val notifier: AlertNotifier
) {
    private val storage = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    suspend fun check(strings: Strings) {
        if (!sessionService.isLoggedIn()) return

        val accountId = sessionService.getAccountId()
        val notificationsResult = alertRepository.getNotifications(accountId)
        if (notificationsResult !is Resource.Success) return

        val pending = notificationsResult.data.orEmpty().filter { !it.confirmed }
        val seen = storage.getStringSet(SEEN_KEY, emptySet()).orEmpty()

        // Primera revision: se marca todo como visto sin avisar
        if (!storage.getBoolean(BASELINE_KEY, false)) {
            storage.edit()
                .putStringSet(SEEN_KEY, pending.map { it.id.toString() }.toSet())
                .putBoolean(BASELINE_KEY, true)
                .apply()
            return
        }

        val fresh = pending.filter { it.id.toString() !in seen }
        if (fresh.isEmpty()) return

        // Se guardan como vistas aunque los avisos esten apagados, para no avisar tarde al encenderlos
        storage.edit().putStringSet(SEEN_KEY, seen + fresh.map { it.id.toString() }).apply()

        if (!preferences.pushEnabled || !notifier.canNotify()) return

        val incidents = incidentRepository.getIncidents(accountId).data.orEmpty().associateBy { it.id }
        val contact = settingsRepository.getEmergencyContact(accountId).data
        val safety = settingsRepository.getSettings(accountId).data?.safety
        val es = strings.isSpanish

        fresh.forEach { notification ->
            val incident = incidents[notification.incidentId]
            val severity = incident?.severity?.ifBlank { null } ?: "Critical"
            val title = if (incident != null) {
                "${trSeverity(strings, severity).uppercase()} · ${trIncidentType(strings, incident.type)}"
            } else {
                "SmartGas"
            }
            val value = incident?.let {
                incidentValueWithLimit(it, safety?.gasThreshold ?: 50.0, safety?.temperatureThreshold ?: 45.0, if (es) "límite" else "limit")
            }
            val text = if (incident != null) {
                "${trZone(strings, incident.zoneName)}${value?.let { ": $it" } ?: ""}. " +
                    if (es) "Toca para ver qué hacer." else "Tap to see what to do."
            } else {
                notification.message
            }

            notifier.show(
                PushContent(
                    notificationId = notification.id,
                    title = title,
                    text = text,
                    critical = severity.equals("critical", ignoreCase = true),
                    contactPhone = contact?.phone.orEmpty(),
                    confirmLabel = if (es) "CONFIRMAR" else "CONFIRM",
                    callLabel = if (es) "LLAMAR CONTACTO" else "CALL CONTACT"
                ),
                vibrate = preferences.vibrateOnCritical
            )
        }
    }

    /** Al cerrar sesion se olvidan las alertas vistas (la siguiente cuenta empieza de cero). */
    fun reset() {
        storage.edit().clear().apply()
    }
}
