package com.smartgas_frontend.shared.utils

enum class TagSeverity { Success, Warning, Danger, Info, Secondary }

fun severityClass(severity: String?): TagSeverity = when (severity) {
    "Warning", "Medium" -> TagSeverity.Warning
    "Critical", "High" -> TagSeverity.Danger
    else -> TagSeverity.Info
}

fun statusClass(status: String?): TagSeverity = when (status) {
    "Safe", "Online", "Active", "Resolved" -> TagSeverity.Success
    "Warning", "Pending", "Detected" -> TagSeverity.Warning
    "Reviewed" -> TagSeverity.Info
    "Critical" -> TagSeverity.Danger
    "Offline", "False Alarm", "Inactive" -> TagSeverity.Secondary
    else -> TagSeverity.Info
}

fun normalizeStatus(status: String?): String =
    status.orEmpty().lowercase().replace(Regex("\\s+"), "").replace("-", "")
