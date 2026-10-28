package com.smartgas_frontend.paymentmanagement.presentation.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.DetailRow
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.formatNumber
import com.smartgas_frontend.shared.utils.trLimit
import com.smartgas_frontend.shared.utils.trPlanDescription
import com.smartgas_frontend.shared.utils.trPlanFeature
import com.smartgas_frontend.shared.utils.trPlanName
import com.smartgas_frontend.shared.utils.trStatus
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.BlueDark
import com.smartgas_frontend.ui.theme.TagSuccess

@Composable
fun PlansScreen(viewModel: PlansViewModel) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val data = state.data ?: SubscriptionData()
    val loading = state.isLoading
    val currentPlan = data.currentPlan
    val subscription = data.subscription
    val selectedPlan = viewModel.selectedPlan.value

    ToastEffect(viewModel.toasts)

    ContentPage {
        PageHeader(title = strings.t(R.string.subscriptionTitle), subtitle = strings.t(R.string.subscriptionSubtitle)) {
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

        if (loading) {
            LoadingState()
        }

        if (!loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(BlueDark, Blue)))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    strings.t(R.string.activePlan).uppercase(),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (currentPlan != null) trPlanName(strings, currentPlan) else "—",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
                if (currentPlan != null) {
                    Text(
                        trPlanDescription(strings, currentPlan),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Tag(
                    value = if (subscription != null) trStatus(strings, subscription.status) else "—",
                    severity = TagSeverity.Success
                )
                if (subscription != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "${strings.t(R.string.renewalDate)}: ${formatDate(subscription.renewalDate)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                strings.t(R.string.availablePlans),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )

            data.plans.forEach { plan ->
                PlanCard(
                    strings = strings,
                    plan = plan,
                    isCurrent = currentPlan != null && currentPlan.id == plan.id,
                    onRequest = { viewModel.openRequest(plan) }
                )
            }
        }
    }

    if (viewModel.showConfirmDialog.value && selectedPlan != null) {
        AlertDialog(
            onDismissRequest = viewModel::closeRequest,
            title = { Text(strings.t(R.string.planChangeTitle)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailRow(
                        strings.t(R.string.fromPlan),
                        if (currentPlan != null) trPlanName(strings, currentPlan) else "—"
                    )
                    DetailRow(strings.t(R.string.toPlan), trPlanName(strings, selectedPlan))
                    DetailRow(
                        strings.t(R.string.price),
                        "PEN ${formatNumber(selectedPlan.price)}${strings.t(R.string.perMonth)}"
                    )

                    Text("${strings.t(R.string.benefits)}:", fontWeight = FontWeight.Bold)
                    selectedPlan.features.forEach { feature -> FeatureRow(trPlanFeature(strings, feature)) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            strings.t(R.string.planChangeNote),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            dismissButton = {
                SecondaryButton(label = strings.t(R.string.cancelAction), onClick = viewModel::closeRequest)
            },
            confirmButton = {
                PrimaryButton(
                    label = strings.t(R.string.confirmRequest),
                    icon = Icons.Outlined.Check,
                    loading = viewModel.confirmLoading.value,
                    onClick = viewModel::confirmRequest
                )
            }
        )
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.Check, contentDescription = null, tint = TagSuccess, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** .plan-card */
@Composable
private fun PlanCard(strings: Strings, plan: Plan, isCurrent: Boolean, onRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            if (isCurrent) 2.dp else 1.dp,
            if (isCurrent) TagSuccess else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isCurrent) {
                Tag(value = strings.t(R.string.activePlan), severity = TagSeverity.Success)
            }

            Text(
                trPlanName(strings, plan),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "PEN ${formatNumber(plan.price)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    strings.t(R.string.perMonth),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                trPlanDescription(strings, plan),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                DetailRow(strings.t(R.string.maxSensors), trLimit(strings, plan.maxSensors, "sensors"))
                DetailRow(strings.t(R.string.maxZones), trLimit(strings, plan.maxZones, "zones"))
                DetailRow(
                    strings.t(R.string.reportLevel),
                    strings.t(if (plan.reportLevel == "Advanced") R.string.advancedReports else R.string.basicReports)
                )
            }

            plan.features.forEach { feature -> FeatureRow(trPlanFeature(strings, feature)) }

            if (!isCurrent) {
                PrimaryButton(
                    label = strings.t(R.string.requestPlanChange),
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    label = strings.t(R.string.activePlan),
                    icon = Icons.Outlined.Check,
                    enabled = false,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
