package com.smartgas_frontend.postincidentprocedures.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.incidentdetection.domain.model.Incident
import com.smartgas_frontend.postincidentprocedures.domain.model.GeneratedReport
import com.smartgas_frontend.postincidentprocedures.domain.model.SecurityReport
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DateField
import com.smartgas_frontend.shared.presentation.components.DetailRow
import com.smartgas_frontend.shared.presentation.components.DropdownField
import com.smartgas_frontend.shared.presentation.components.EmptyMessage
import com.smartgas_frontend.shared.presentation.components.MetricCard
import com.smartgas_frontend.shared.presentation.components.MetricTone
import com.smartgas_frontend.shared.presentation.components.MetricsGrid
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.normalizeStatus
import com.smartgas_frontend.shared.utils.trIncidentType
import com.smartgas_frontend.shared.utils.trSeverity
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.shared.utils.trZone
import com.smartgas_frontend.ui.theme.Green
import com.smartgas_frontend.ui.theme.TagDanger
import com.smartgas_frontend.ui.theme.TagInfo
import com.smartgas_frontend.ui.theme.TagSuccess
import com.smartgas_frontend.ui.theme.TagWarning

private const val ROWS_PER_PAGE = 10

private fun sevSeverity(severity: String?): TagSeverity = when (severity) {
    "Warning", "High" -> TagSeverity.Warning
    "Critical" -> TagSeverity.Danger
    else -> TagSeverity.Info
}

private fun statusSeverity(status: String?): TagSeverity = when (normalizeStatus(status)) {
    "active", "detected" -> TagSeverity.Warning
    "reviewed" -> TagSeverity.Info
    "resolved" -> TagSeverity.Success
    "falsealarm" -> TagSeverity.Secondary
    else -> TagSeverity.Info
}

private fun barColor(severity: String): Color = when (severity.lowercase()) {
    "critical" -> TagDanger
    "warning", "high" -> TagWarning
    else -> TagInfo
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val report = state.data ?: SecurityReport()
    val loading = state.isLoading
    val filters = viewModel.filters.value
    val reportSummary = viewModel.reportSummary.value
    val generatedReport = viewModel.generatedReport.value
    val filteredIncidents = viewModel.filteredIncidents

    val zoneOptions = listOf(strings.t(R.string.allZonesOption) to "") +
        report.zones.map { trZone(strings, it.name) to it.name }

    val severityOptions = listOf(strings.t(R.string.allSeverities) to "") +
        listOf("Warning", "High", "Critical").map { trSeverity(strings, it) to it }

    val statusOptions = listOf(
        strings.t(R.string.allStatuses) to "",
        trStatus(strings, "Active") to "Active",
        trStatus(strings, "Detected") to "Detected",
        trStatus(strings, "Reviewed") to "Reviewed",
        trStatus(strings, "Resolved") to "Resolved",
        trStatus(strings, "False Alarm") to "FalseAlarm"
    )

    val typeOptions = listOf(strings.t(R.string.allTypes) to "") +
        viewModel.incidentTypes.ifEmpty { listOf("Gas Leak", "High Temperature") }
            .map { trIncidentType(strings, it) to it }

    ContentPage {
        PageHeader(title = strings.t(R.string.reportsTitle), subtitle = strings.t(R.string.reportsSubtitle)) {
            SecondaryButton(
                label = strings.t(R.string.refreshAction),
                icon = Icons.Outlined.Refresh,
                loading = loading,
                onClick = viewModel::loadData
            )
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (!loading) {
            MetricsGrid(
                listOf(
                    { modifier ->
                        MetricCard(
                            icon = Icons.AutoMirrored.Outlined.List,
                            value = report.incidents.size.toString(),
                            label = strings.t(R.string.totalIncidents),
                            modifier = modifier
                        )
                    },
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.Notifications,
                            value = viewModel.pendingCount.toString(),
                            label = strings.t(R.string.pendingAlerts),
                            tone = MetricTone.Orange,
                            modifier = modifier
                        )
                    },
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.CheckCircle,
                            value = viewModel.resolvedCount.toString(),
                            label = strings.t(R.string.resolved),
                            modifier = modifier
                        )
                    },
                    { modifier ->
                        MetricCard(
                            icon = Icons.Outlined.LocationOn,
                            value = trZone(strings, viewModel.mostAffectedZone),
                            label = strings.t(R.string.mostAffectedZone),
                            modifier = modifier
                        )
                    }
                )
            )

            PanelCard(title = strings.t(R.string.filters), icon = Icons.Outlined.FilterAlt) {
                DropdownField(
                    label = strings.t(R.string.filterZone),
                    options = zoneOptions,
                    selected = filters.zone,
                    onSelected = { viewModel.onFiltersChanged(filters.copy(zone = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = strings.t(R.string.filterSeverity),
                    options = severityOptions,
                    selected = filters.severity,
                    onSelected = { viewModel.onFiltersChanged(filters.copy(severity = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = strings.t(R.string.filterStatus),
                    options = statusOptions,
                    selected = filters.status,
                    onSelected = { viewModel.onFiltersChanged(filters.copy(status = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = strings.t(R.string.filterType),
                    options = typeOptions,
                    selected = filters.type,
                    onSelected = { viewModel.onFiltersChanged(filters.copy(type = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                DateField(
                    label = strings.t(R.string.filterDateFrom),
                    value = filters.from,
                    onValueChange = { viewModel.onFiltersChanged(filters.copy(from = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                DateField(
                    label = strings.t(R.string.filterDateTo),
                    value = filters.to,
                    onValueChange = { viewModel.onFiltersChanged(filters.copy(to = it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        label = strings.t(R.string.applyFilters),
                        icon = Icons.Outlined.FilterAlt,
                        onClick = { viewModel.applyFilters(strings) }
                    )
                    SecondaryButton(
                        label = strings.t(R.string.clearFilters),
                        icon = Icons.Outlined.Close,
                        onClick = viewModel::clearFilters
                    )
                    PrimaryButton(
                        label = strings.t(R.string.generateReport),
                        icon = Icons.Outlined.Description,
                        containerColor = TagSuccess,
                        onClick = { viewModel.generateReport(strings) }
                    )
                }

                if (reportSummary.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(reportSummary, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                if (generatedReport != null) {
                    GeneratedReportCard(strings, generatedReport)
                }
            }

            PanelCard(title = strings.t(R.string.incidentsBySeverity)) {
                viewModel.countBySeverity.forEach { (severity, count) ->
                    AnalyticsRow(
                        count = count,
                        total = filteredIncidents.size,
                        color = barColor(severity)
                    ) {
                        Tag(value = trSeverity(strings, severity), severity = sevSeverity(severity))
                    }
                }

                if (filteredIncidents.isEmpty()) {
                    EmptyMessage(strings.t(R.string.noData))
                }
            }

            PanelCard(title = strings.t(R.string.incidentsByType)) {
                viewModel.countByType.forEach { (type, count) ->
                    AnalyticsRow(count = count, total = filteredIncidents.size, color = TagInfo) {
                        Text(
                            trIncidentType(strings, type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (filteredIncidents.isEmpty()) {
                    EmptyMessage(strings.t(R.string.noData))
                }
            }

            IncidentHistory(strings, filteredIncidents)
        }
    }
}

@Composable
private fun GeneratedReportCard(strings: Strings, report: GeneratedReport) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, Green)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                strings.t(R.string.reportGeneratedTitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                report.periodLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DetailRow(strings.t(R.string.reportScope), report.zoneLabel)
            DetailRow(strings.t(R.string.filteredIncidents), report.total.toString())
            DetailRow(strings.t(R.string.criticalIncidents), report.critical.toString())
            DetailRow(strings.t(R.string.warningIncidents), report.warning.toString())
            DetailRow(strings.t(R.string.pendingIncidents), report.pending.toString())
            DetailRow(strings.t(R.string.resolvedIncidents), report.resolved.toString())
            DetailRow(strings.t(R.string.mostAffectedZone), report.mostAffectedZone)

            Text(
                "${strings.t(R.string.reportRecommendation)}: ${strings.t(R.string.reportRecommendationText)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** .analytics-row: etiqueta + barra proporcional + cantidad. */
@Composable
private fun AnalyticsRow(count: Int, total: Int, color: Color, label: @Composable () -> Unit) {
    val fraction = (count.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.width(120.dp)) { label() }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Text(count.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Historial paginado (DataTable con paginator de 10 filas en la web). */
@Composable
private fun IncidentHistory(strings: Strings, incidents: List<Incident>) {
    var page by remember(incidents.size) { mutableIntStateOf(0) }
    val pageCount = ((incidents.size + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE).coerceAtLeast(1)
    val rows = incidents.drop(page * ROWS_PER_PAGE).take(ROWS_PER_PAGE)

    PanelCard(title = "${strings.t(R.string.incidentHistory)} (${incidents.size})") {
        rows.forEach { incident ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(incident.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Tag(value = trSeverity(strings, incident.severity), severity = sevSeverity(incident.severity))
                        Tag(value = trStatus(strings, incident.status), severity = statusSeverity(incident.status))
                    }
                }
                DetailRow(strings.t(R.string.incidentType), trIncidentType(strings, incident.type))
                DetailRow(strings.t(R.string.incidentZone), trZone(strings, incident.zoneName))
                DetailRow(strings.t(R.string.detectedValue), incident.detectedValue)
                DetailRow(strings.t(R.string.detectedAt), formatDate(incident.detectedAt))
            }
        }

        if (incidents.isEmpty()) {
            EmptyMessage(strings.t(R.string.noData))
        }

        if (incidents.size > ROWS_PER_PAGE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryButton(label = "‹", enabled = page > 0, onClick = { page-- })
                Text("${page + 1} / $pageCount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                SecondaryButton(label = "›", enabled = page < pageCount - 1, onClick = { page++ })
            }
        }
    }
}
