package com.smartgas_frontend.kitchenmonitoring.presentation.components

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Diseno 09: escaner QR con ML Kit (Google code scanner).
 * Google Play Services abre su propia pantalla de camara y devuelve el texto del QR,
 * asi que la app no necesita pedir el permiso de camara.
 */
fun startQrScan(
    context: Context,
    onResult: (String) -> Unit,
    onCancel: () -> Unit = {},
    onError: (Exception) -> Unit = {}
) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .enableAutoZoom()
        .build()

    GmsBarcodeScanning.getClient(context, options)
        .startScan()
        .addOnSuccessListener { barcode -> barcode.rawValue?.let(onResult) ?: onCancel() }
        .addOnCanceledListener { onCancel() }
        .addOnFailureListener { error -> onError(error) }
}
