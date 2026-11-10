package com.smartgas_frontend.shared.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val shortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm")

fun parseDate(isoString: String?): Instant? {
    if (isoString.isNullOrBlank()) return null
    return runCatching { Instant.parse(isoString) }
        .recoverCatching { OffsetDateTime.parse(isoString).toInstant() }
        .recoverCatching { LocalDateTime.parse(isoString).atZone(ZoneId.systemDefault()).toInstant() }
        .recoverCatching { LocalDate.parse(isoString).atStartOfDay(ZoneOffset.UTC).toInstant() }
        .getOrNull()
}

fun dateMillis(isoString: String?): Long = parseDate(isoString)?.toEpochMilli() ?: 0L

fun formatDate(isoString: String?): String {
    val instant = parseDate(isoString) ?: return "—"
    return shortFormatter.format(instant.atZone(ZoneId.systemDefault()))
}

fun nowISO(): String = Instant.now().toString()

fun formatNumber(value: Double?): String {
    if (value == null) return "—"
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}

/** Hora corta de las listas (disenos 10 y 14): "10:42" si es de hoy, si no "Oct 05, 10:42". */
fun formatListDateTime(isoString: String?, spanish: Boolean): String {
    val instant = parseDate(isoString) ?: return "—"
    val dateTime = instant.atZone(ZoneId.systemDefault())
    val locale = if (spanish) java.util.Locale("es") else java.util.Locale.US

    return if (dateTime.toLocalDate() == LocalDate.now()) {
        DateTimeFormatter.ofPattern("HH:mm", locale).format(dateTime)
    } else {
        DateTimeFormatter.ofPattern("MMM dd, HH:mm", locale).format(dateTime)
    }
}

/** Duracion corta: "45s", "4m", "2h 5m", "3d 4h". */
fun formatDuration(millis: Long): String {
    val minutes = millis / 60_000
    return when {
        minutes < 1 -> "${millis / 1000}s"
        minutes < 60 -> "${minutes}m"
        minutes < 60 * 24 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "${minutes / (60 * 24)}d ${(minutes / 60) % 24}h"
    }
}
