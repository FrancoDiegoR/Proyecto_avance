package com.smartgas_frontend.shared.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre el marcador del telefono con el numero ya escrito.
 * ACTION_DIAL no llama directamente, por eso no necesita el permiso CALL_PHONE.
 * Devuelve false si el dispositivo no tiene app de telefono.
 */
fun dialPhone(context: Context, phone: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    true
} catch (e: ActivityNotFoundException) {
    false
}
