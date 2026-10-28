package com.smartgas_frontend.iam.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.iam.data.remote.ProfileService
import com.smartgas_frontend.iam.data.remote.dto.UpdateProfileRequest
import com.smartgas_frontend.iam.data.remote.dto.toProfile
import com.smartgas_frontend.iam.domain.model.AccountActivity
import com.smartgas_frontend.iam.domain.model.Profile
import com.smartgas_frontend.iam.domain.model.ProfileStats
import com.smartgas_frontend.incidentdetection.data.remote.IncidentService
import com.smartgas_frontend.incidentdetection.data.remote.dto.toIncident
import com.smartgas_frontend.kitchenmonitoring.data.remote.SensorService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensor
import com.smartgas_frontend.kitchenmonitoring.data.remote.MonitoringService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toZone
import com.smartgas_frontend.paymentmanagement.data.remote.SubscriptionService
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toPlan
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toSubscription
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.remote.bodyOrNull
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class ProfileDraft(
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val accountType: String = "",
    val businessName: String = "",
    val phone: String = "",
    val district: String = "",
    val address: String = ""
)

class ProfileRepository(
    private val service: ProfileService,
    private val subscriptionService: SubscriptionService,
    private val sensorService: SensorService,
    private val monitoringService: MonitoringService,
    private val incidentService: IncidentService,
    private val sessionService: SessionService
) {

    suspend fun getProfile(accountId: Int): Resource<Profile> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val profileResponse = async { service.getProfile(accountId).bodyOrThrow() }
                val subscriptionResponse = async {
                    runCatching { subscriptionService.getCurrentSubscription(accountId).bodyOrNull() }.getOrNull()
                }

                profileResponse.await().toProfile(
                    session = sessionService.getCurrentUser(),
                    subscription = subscriptionResponse.await()?.toSubscription()
                )
            }
        }
    }

    suspend fun updateProfile(accountId: Int, data: ProfileDraft): Resource<Profile> = withContext(Dispatchers.IO) {
        safeCall {
            val response = service.updateProfile(
                accountId,
                UpdateProfileRequest(
                    fullName = data.fullName,
                    businessName = data.businessName,
                    phone = data.phone,
                    district = data.district,
                    address = data.address
                )
            )

            response.bodyOrThrow().toProfile(sessionService.getCurrentUser())
        }
    }

    suspend fun getPlan(planId: Int?): Resource<Plan?> = withContext(Dispatchers.IO) {
        safeCall {
            if (planId == null) {
                null
            } else {
                subscriptionService.getPlans().bodyOrThrow().map { it.toPlan() }.find { it.id == planId }
            }
        }
    }

    suspend fun getSubscription(accountId: Int): Resource<Subscription> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val plansResponse = async { subscriptionService.getPlans().bodyOrThrow() }
                val subscriptionResponse = async { subscriptionService.getCurrentSubscription(accountId).bodyOrThrow() }

                subscriptionResponse.await().toSubscription(plansResponse.await().map { it.toPlan() })
            }
        }
    }

    suspend fun getActivity(accountId: Int): Resource<List<AccountActivity>> = Resource.Success(emptyList())

    suspend fun getStats(accountId: Int): Resource<ProfileStats> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val sensors = async { sensorService.getSensors(accountId).bodyOrThrow() }
                val zones = async { monitoringService.getZones(accountId).bodyOrThrow() }
                val incidents = async { incidentService.getIncidents(accountId).bodyOrThrow() }

                ProfileStats(
                    sensors = sensors.await().map { it.toSensor() },
                    zones = zones.await().map { it.toZone() },
                    activeIncidents = incidents.await()
                        .map { it.toIncident() }
                        .filter { it.status == "Active" || it.status == "Reviewed" }
                )
            }
        }
    }

    suspend fun changePassword(accountId: Int, password: String): Resource<Unit> =
        Resource.Error("Password change is not available in the current backend.")
}
