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
