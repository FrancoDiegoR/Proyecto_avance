package com.smartgas_frontend.incidentpreventionnotification.presentation.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smartgas_frontend.MainActivity
import com.smartgas_frontend.R

private const val CHANNEL_ID = "smartgas_alerts"

/** Extra del Intent con el id de la notificacion del backend: MainActivity abre su Alert detail. */
const val EXTRA_ALERT_ID = "smartgas_alert_id"

/** Lo que muestra el aviso: "CRITICAL · Gas leak detected" + "Gas cylinder storage: 78 ppm (limit 50)...". */
data class PushContent(
    val notificationId: Int,
    val title: String,
    val text: String,
    val critical: Boolean,
    val contactPhone: String,
    val confirmLabel: String,
    val callLabel: String
)

/**
 * Diseno 11: muestra la alerta como notificacion del sistema, con los botones CONFIRM y CALL CONTACT.
 * Tocar la notificacion abre la app en el detalle de la alerta (diseno 12).
 */
class AlertNotifier(private val context: Context) {

    /** El canal se crea una vez; Android recuerda si el usuario lo silencio. */
    fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "SmartGas alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Gas and temperature alerts"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Desde Android 13 hace falta el permiso POST_NOTIFICATIONS. */
    fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(content: PushContent, vibrate: Boolean) {
        if (!canNotify()) return

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        // Tocar la notificacion -> abre la app en el detalle de la alerta
        val openIntent = Intent(context, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALERT_ID, content.notificationId)
        }
        val openPending = PendingIntent.getActivity(context, content.notificationId, openIntent, flags)

        // Boton CONFIRM -> AlertActionReceiver confirma en el backend sin abrir la app
        val confirmIntent = Intent(context, AlertActionReceiver::class.java).apply {
            action = ACTION_CONFIRM
            putExtra(EXTRA_ALERT_ID, content.notificationId)
        }
        val confirmPending = PendingIntent.getBroadcast(context, content.notificationId, confirmIntent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(0, content.confirmLabel, confirmPending)

        // Boton CALL CONTACT -> marcador del telefono con el numero del contacto de emergencia
        if (content.contactPhone.isNotBlank()) {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${content.contactPhone}"))
            val callPending = PendingIntent.getActivity(context, content.notificationId + 100_000, callIntent, flags)
            builder.addAction(0, content.callLabel, callPending)
        }

        try {
            NotificationManagerCompat.from(context).notify(content.notificationId, builder.build())
        } catch (e: SecurityException) {
            // El usuario quito el permiso mientras tanto: no se muestra nada
        }

        // "Vibrate on critical alerts" (diseno 15)
        if (vibrate && content.critical) {
            context.getSystemService(Vibrator::class.java)
                ?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 400), -1))
        }
    }

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
