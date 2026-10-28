package com.smartgas_frontend.shared.data.remote

import com.google.gson.JsonElement

const val UNLIMITED = Int.MAX_VALUE

fun JsonElement?.asTextOrNull(): String? {
    if (this == null || isJsonNull || !isJsonPrimitive) return null
    val primitive = asJsonPrimitive
    if (primitive.isNumber) {
        val number = primitive.asDouble
        return if (number % 1.0 == 0.0) number.toLong().toString() else number.toString()
    }
    return primitive.asString
}

fun JsonElement?.asLimit(): Int? {
    val text = asTextOrNull() ?: return null
    if (text.equals("Unlimited", ignoreCase = true)) return UNLIMITED
    return text.toDoubleOrNull()?.toInt()
}

fun JsonElement?.asFeatureList(): List<String> {
    if (this == null || isJsonNull) return emptyList()
    if (isJsonArray) return asJsonArray.mapNotNull { it.asTextOrNull() }
    return asTextOrNull().orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

fun JsonElement?.asStringMap(): Map<String, String> {
    if (this == null || !isJsonObject) return emptyMap()
    return asJsonObject.entrySet().mapNotNull { (key, value) ->
        value.asTextOrNull()?.let { key to it }
    }.toMap()
}
