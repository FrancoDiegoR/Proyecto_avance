package com.smartgas_frontend.kitchenmonitoring.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.Orange
import com.smartgas_frontend.ui.theme.Red
import com.smartgas_frontend.ui.theme.TagSuccess

// Icono y color de cada zona en las listas (disenos 04 y 05).

/** Icono segun el nombre de la zona (cocina, freidora, almacen de gas, bar...). */
fun zoneIcon(name: String): ImageVector {
    val text = name.lowercase()
    return when {
        listOf("fryer", "freidora", "stove", "cocina caliente").any { it in text } -> Icons.Outlined.LocalFireDepartment
        listOf("storage", "cylinder", "warehouse", "almac", "balón", "balon", "dep").any { it in text } -> Icons.Outlined.Inventory2
        listOf("bar", "dessert", "postre").any { it in text } -> Icons.Outlined.LocalBar
        listOf("dining", "comedor", "salón", "salon").any { it in text } -> Icons.Outlined.Restaurant
        listOf("kitchen", "cocina").any { it in text } -> Icons.Outlined.Kitchen
        else -> Icons.Outlined.Sensors
    }
}

/** Verde seguro, naranja advertencia, rojo critico; azul si el estado es desconocido. */
fun zoneStatusColor(status: String?): Color = when (normalizeStatus(status)) {
    "safe", "normal", "online" -> TagSuccess
    "warning", "active" -> Orange
    "critical" -> Red
    else -> Blue
}
