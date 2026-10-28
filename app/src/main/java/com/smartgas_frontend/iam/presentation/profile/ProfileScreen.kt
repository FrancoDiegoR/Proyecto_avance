package com.smartgas_frontend.iam.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartgas_frontend.R
import com.smartgas_frontend.iam.domain.model.AccountActivity
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.i18n.Strings
import com.smartgas_frontend.shared.presentation.components.AlertBanner
import com.smartgas_frontend.shared.presentation.components.ContentPage
import com.smartgas_frontend.shared.presentation.components.IconBadge
import com.smartgas_frontend.shared.presentation.components.LoadingState
import com.smartgas_frontend.shared.presentation.components.PageHeader
import com.smartgas_frontend.shared.presentation.components.PanelCard
import com.smartgas_frontend.shared.presentation.components.PrimaryButton
import com.smartgas_frontend.shared.presentation.components.SecondaryButton
import com.smartgas_frontend.shared.presentation.components.Tag
import com.smartgas_frontend.shared.presentation.components.ToastEffect
import com.smartgas_frontend.shared.presentation.navigation.Routes
import com.smartgas_frontend.shared.utils.TagSeverity
import com.smartgas_frontend.shared.utils.dateMillis
import com.smartgas_frontend.shared.utils.formatDate
import com.smartgas_frontend.shared.utils.nowISO
import com.smartgas_frontend.shared.utils.trAccountType
import com.smartgas_frontend.shared.utils.trPlanDescription
import com.smartgas_frontend.shared.utils.trPlanName
import com.smartgas_frontend.ui.theme.Blue
import com.smartgas_frontend.ui.theme.BlueDark

private fun initials(fullName: String): String =
    fullName.ifEmpty { "SG" }
        .split(' ')
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().toString() }
        .uppercase()

private fun latestDate(dates: List<String?>, fallback: String?): String? =
    dates.filter { dateMillis(it) > 0 }.maxByOrNull { dateMillis(it) } ?: fallback

private fun accountActivity(strings: Strings, data: ProfileData): List<AccountActivity> {
    val profile = data.profile
    val subscription = data.subscription
    val stats = data.stats
    val profileDate = profile?.updatedAt ?: profile?.createdAt
    val rows = mutableListOf<AccountActivity>()

    if (data.plan != null || subscription != null) {
        rows.add(
            AccountActivity(
                id = "plan-current",
                title = strings.t(R.string.activityCurrentPlan),
                detail = if (data.plan != null) trPlanName(strings, data.plan) else trPlanName(strings, subscription?.planName ?: "Basic"),
                createdAt = subscription?.updatedAt ?: subscription?.startDate ?: subscription?.renewalDate ?: profileDate
            )
        )
    }

    rows.add(
        AccountActivity(
            id = "sensors-summary",
            title = strings.t(R.string.activitySensorsSummary),
            detail = strings.t(R.string.activitySensorsSummaryDetail, "count" to stats.sensors.size),
            createdAt = latestDate(stats.sensors.map { it.updatedAt ?: it.createdAt }, profileDate ?: nowISO())
        )
    )

    rows.add(
        AccountActivity(
            id = "zones-summary",
            title = strings.t(R.string.activityZonesSummary),
            detail = strings.t(R.string.activityZonesSummaryDetail, "count" to stats.zones.size),
            createdAt = latestDate(stats.zones.map { it.updatedAt ?: it.createdAt ?: it.lastUpdated }, profileDate ?: nowISO())
        )
    )

    val latestIncident = stats.activeIncidents.maxByOrNull { dateMillis(it.detectedAt) }

    if (latestIncident != null) {
        rows.add(
            AccountActivity(
                id = "latest-incident",
                title = strings.t(R.string.activityLatestIncident),
                detail = "${latestIncident.code.ifEmpty { "INC" }} · ${latestIncident.type}",
                createdAt = latestIncident.detectedAt ?: latestIncident.updatedAt
            )
        )
    } else {
        rows.add(
            AccountActivity(
                id = "no-active-incidents",
                title = strings.t(R.string.activityNoActiveIncidents),
                detail = strings.t(R.string.activityNoActiveIncidentsDetail),
                createdAt = profileDate
            )
        )
    }

    return rows.filter { it.title.isNotEmpty() }.take(5)
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val strings = LocalStrings.current
    val state = viewModel.state.value
    val data = state.data ?: ProfileData()
    val loading = state.isLoading
    val profile = data.profile
    val form = viewModel.form.value
    val editing = viewModel.editing.value
    val sessionMissing = viewModel.sessionMissing.value

    LaunchedEffect(sessionMissing) {
        if (sessionMissing) onLogout()
    }

    ToastEffect(viewModel.toasts)

    ContentPage {
        PageHeader(title = strings.t(R.string.profileTitle), subtitle = strings.t(R.string.profileSubtitle)) {
            if (!editing) {
                PrimaryButton(label = strings.t(R.string.editAction), icon = Icons.Outlined.Edit, onClick = viewModel::startEdit)
            } else {
                PrimaryButton(
                    label = strings.t(R.string.saveAction),
                    icon = Icons.Outlined.Check,
                    loading = viewModel.saveLoading.value,
                    onClick = viewModel::saveProfile
                )
                SecondaryButton(label = strings.t(R.string.cancelAction), onClick = viewModel::cancelEdit)
            }
        }

        if (state.message.isNotEmpty()) {
            AlertBanner(strings.t(R.string.apiError))
        }

        if (loading) {
            LoadingState()
        }

        if (!loading && profile != null) {
            // .profile-hero-card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(BlueDark, Blue)))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials(form.fullName), color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                    Column {
                        Text(
                            profile.fullName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            "${trAccountType(strings, form.accountType)} · ${trPlanName(strings, data.plan)}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(value = strings.t(R.string.activeAccount), severity = TagSeverity.Success)
                    Text(
                        "${strings.t(R.string.memberSince)} ${profile.memberSince?.take(10) ?: "2026"}",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroMetric(data.stats.sensors.size, strings.t(R.string.devices), Modifier.weight(1f))
                    HeroMetric(data.stats.zones.size, strings.t(R.string.monitoredZones), Modifier.weight(1f))
                    HeroMetric(data.stats.activeIncidents.size, strings.t(R.string.activeIncidents), Modifier.weight(1f))
                }
            }

            PanelCard(title = strings.t(R.string.personalInformation)) {
                ProfileInfoRow(
                    icon = Icons.Outlined.Person,
                    label = strings.t(R.string.fullName),
                    value = form.fullName,
                    editing = editing,
                    onValueChange = { viewModel.onFormChanged(form.copy(fullName = it)) }
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.Email,
                    label = strings.t(R.string.email),
                    value = form.email,
                    trailing = { Tag(value = strings.t(R.string.verified), severity = TagSeverity.Success) }
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.Work,
                    label = strings.t(R.string.accountType),
                    value = trAccountType(strings, form.accountType)
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.Business,
                    label = strings.t(R.string.businessName),
                    value = form.businessName,
                    editing = editing,
                    onValueChange = { viewModel.onFormChanged(form.copy(businessName = it)) }
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.Phone,
                    label = strings.t(R.string.phone),
                    value = form.phone,
                    editing = editing,
                    onValueChange = { viewModel.onFormChanged(form.copy(phone = it)) }
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.LocationOn,
                    label = strings.t(R.string.district),
                    value = form.district,
                    editing = editing,
                    onValueChange = { viewModel.onFormChanged(form.copy(district = it)) }
                )
            }

            PanelCard(title = strings.t(R.string.accountSecurity)) {
                ProfileInfoRow(
                    icon = Icons.Outlined.Lock,
                    label = strings.t(R.string.verified),
                    value = strings.t(R.string.password),
                    trailing = { Tag(value = strings.t(R.string.verified), severity = TagSeverity.Success) }
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.Computer,
                    label = "Android · Lima, PE",
                    value = strings.t(R.string.sessionDevices),
                    trailing = { Tag(value = strings.t(R.string.active), severity = TagSeverity.Success) }
                )
            }

            // .plan-profile-card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconBadge(Icons.Outlined.CreditCard)
                    Text(
                        trPlanName(strings, data.plan),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        trPlanDescription(strings, data.plan),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (data.subscription != null) {
                        Text(
                            "${strings.t(R.string.renewalDate)}: ${formatDate(data.subscription.renewalDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    SecondaryButton(
                        label = strings.t(R.string.viewPlanDetails),
                        onClick = { onNavigate(Routes.PLANS) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            PanelCard(title = strings.t(R.string.recentActivity)) {
                accountActivity(strings, data).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                item.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            formatDate(item.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value.toString(), color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

/** .profile-info-row / .security-row */
@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    editing: Boolean = false,
    onValueChange: ((String) -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconBadge(icon)

        if (editing && onValueChange != null) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value.ifEmpty { "—" }, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            trailing?.invoke()
        }
    }
}
