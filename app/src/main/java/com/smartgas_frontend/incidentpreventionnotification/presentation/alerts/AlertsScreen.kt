package com.smartgas_frontend.incidentpreventionnotification.presentation.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.incidentpreventionnotification.domain.model.Notification
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DetailRow
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatIncidentNotification
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.TagSuccess

/** Textos propios de la pagina (la web los define en `pageText`, fuera de los locales). */
private class PageText(es: Boolean) {
    val title = if (es) "Alertas" else "Alerts"
    val subtitle =
        if (es) "Las alertas también se envían como notificaciones push."
        else "Alerts are also sent as push notifications."
    val refresh = if (es) "Actualizar" else "Refresh"

    val channel = if (es) "Canal" else "Channel"
    val read = if (es) "Leído" else "Read"
    val confirmed = if (es) "Confirmado" else "Confirmed"
    val detectedAt = if (es) "Detectado" else "Detected At"

    val markAsRead = if (es) "Marcar como leído" else "Mark as read"
    val confirmReceipt = if (es) "Confirmar recepción" else "Confirm receipt"

    val yes = if (es) "Sí" else "Yes"
    val no = "No"

    val noNotifications = if (es) "No hay notificaciones disponibles." else "No notifications available."
}

private val incidentMessageRegex =
    Regex("^Sensor (.+?) detected a (.+?) event in zone (.+?)\\.$", RegexOption.IGNORE_CASE)

private fun notificationMessage(strings: Strings, notification: Notification): String {
    val translated = formatIncidentNotification(strings, notification)

    if (translated.isNotEmpty() && translated != notification.message) {
        return translated
    }

    val match = incidentMessageRegex.find(notification.message)

    if (match != null) {
        val sensor = match.groupValues[1]
        val type = trIncidentType(strings, match.groupValues[2])
        val zone = trZone(strings, match.groupValues[3])

        return if (strings.isSpanish) {
            "El sensor $sensor detectó un evento de $type en la zona $zone."
        } else {
            "Sensor $sensor detected a $type event in zone $zone."
        }
    }

    return notification.message.ifEmpty { "—" }
}

@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val strings = LocalStrings.current
    val pageText = PageText(strings.isSpanish)
    val state = viewModel.state.value
    val notifications = state.data.orEmpty()
    val loading = state.isLoading

    ToastEffect(viewModel.toasts)

    ContentPage {
        PageHeader(title = pageText.title, subtitle = pageText.subtitle) {
            SecondaryButton(
                label = pageText.refresh,
                icon = Icons.Outlined.Refresh,
                loading = loading,
                onClick = viewModel::loadData
            )
        }

        if (loading) {
            LoadingState()
        }

        notifications.forEach { notification ->
            NotificationCard(
                strings = strings,
                pageText = pageText,
                notification = notification,
                onMarkRead = { viewModel.markNotificationRead(notification) },
                onConfirm = { viewModel.confirmNotification(notification) }
            )
        }

        if (!loading && notifications.isEmpty()) {
            PanelCard { EmptyMessage(pageText.noNotifications) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationCard(
    strings: Strings,
    pageText: PageText,
    notification: Notification,
    onMarkRead: () -> Unit,
    onConfirm: () -> Unit
) {
    PanelCard {
        Text(notificationMessage(strings, notification), color = MaterialTheme.colorScheme.onSurface)

        Column {
            DetailRow(pageText.channel, notification.channel)
            DetailRow(pageText.read) {
                Tag(
                    value = if (notification.read) pageText.yes else pageText.no,
                    severity = if (notification.read) TagSeverity.Success else TagSeverity.Danger
                )
            }
            DetailRow(pageText.confirmed) {
                Tag(
                    value = if (notification.confirmed) pageText.yes else pageText.no,
                    severity = if (notification.confirmed) TagSeverity.Success else TagSeverity.Secondary
                )
            }
            DetailRow(pageText.detectedAt, formatDate(notification.createdAt))
        }

        if (!notification.read || !notification.confirmed) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!notification.read) {
                    SecondaryButton(label = pageText.markAsRead, onClick = onMarkRead)
                }
                if (!notification.confirmed) {
                    PrimaryButton(label = pageText.confirmReceipt, onClick = onConfirm, containerColor = TagSuccess)
                }
            }
        }
    }
}
