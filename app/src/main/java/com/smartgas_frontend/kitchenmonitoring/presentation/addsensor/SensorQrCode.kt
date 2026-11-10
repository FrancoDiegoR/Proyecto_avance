package com.smartgas_frontend.kitchenmonitoring.presentation.addsensor

import com.google.gson.JsonParser

/** Datos que trae la etiqueta QR del sensor. Solo el codigo es obligatorio. */
data class ScannedSensor(
    val code: String,
    val type: String? = null,
    val name: String? = null
)

/**
 * Lee el texto del QR. Acepta tres formatos:
 * 1. JSON:   {"code":"GLP-ST-03","type":"Gas","name":"Storage · back wall"}
 * 2. Campos: SMARTGAS;code=GLP-ST-03;type=Gas;name=Storage
 * 3. Texto simple: GLP-ST-03 (solo el codigo)
 * Devuelve null si el QR esta vacio.
 */
fun parseSensorQr(raw: String): ScannedSensor? {
    val text = raw.trim()
    if (text.isEmpty()) return null

    if (text.startsWith("{")) {
        return try {
            val json = JsonParser.parseString(text).asJsonObject
            val code = json.get("code")?.asString?.trim().orEmpty()
            if (code.isEmpty()) null
            else ScannedSensor(
                code = code,
                type = json.get("type")?.asString?.trim(),
                name = json.get("name")?.asString?.trim()
            )
        } catch (e: Exception) {
            null
        }
    }

    if (text.contains('=')) {
        val fields = text.split(';')
            .mapNotNull { part -> part.split('=', limit = 2).takeIf { it.size == 2 } }
            .associate { (key, value) -> key.trim().lowercase() to value.trim() }
        val code = fields["code"].orEmpty()
        return if (code.isEmpty()) null else ScannedSensor(code, fields["type"], fields["name"])
    }

    return ScannedSensor(code = text)
}
