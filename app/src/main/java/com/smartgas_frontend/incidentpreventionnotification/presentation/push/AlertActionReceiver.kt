package com.smartgas_frontend.incidentpreventionnotification.presentation.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartgas_frontend.incidentpreventionnotification.data.di.IncidentPreventionNotificationDataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

const val ACTION_CONFIRM = "com.smartgas_frontend.action.CONFIRM_ALERT"

/**
 * Boton CONFIRM de la notificacion (diseno 11): confirma la alerta en el backend
 * sin abrir la app y luego quita la notificacion.
 */
class AlertActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CONFIRM) return
        val notificationId = intent.getIntExtra(EXTRA_ALERT_ID, -1)
        if (notificationId < 0) return

        // goAsync() mantiene vivo el receiver mientras termina la llamada a la API
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                IncidentPreventionNotificationDataModule.getAlertRepository().confirmNotification(notificationId)
                AlertNotifier(context).cancel(notificationId)
            } finally {
                pending.finish()
            }
        }
    }
}
