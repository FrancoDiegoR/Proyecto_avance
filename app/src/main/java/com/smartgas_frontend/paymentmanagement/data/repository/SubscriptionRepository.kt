package com.smartgas_frontend.paymentmanagement.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.paymentmanagement.data.remote.SubscriptionService
import com.smartgas_frontend.paymentmanagement.data.remote.dto.ChangePlanRequest
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toPlan
import com.smartgas_frontend.paymentmanagement.data.remote.dto.toSubscription
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription
import com.smartgas_frontend.paymentmanagement.domain.model.SubscriptionRequest
import com.smartgas_frontend.shared.data.remote.DomainException
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

const val PLAN_REQUIRED = "PLAN_REQUIRED"

class SubscriptionRepository(private val service: SubscriptionService) {

    private suspend fun fetchPlans(): List<Plan> =
        service.getPlans().bodyOrThrow().map { it.toPlan() }

    suspend fun getPlans(): Resource<List<Plan>> = withContext(Dispatchers.IO) {
        safeCall { fetchPlans() }
    }

    suspend fun getSubscription(accountId: Int): Resource<Subscription> = withContext(Dispatchers.IO) {
        safeCall {
            coroutineScope {
                val plans = async { fetchPlans() }
                val subscription = async { service.getCurrentSubscription(accountId).bodyOrThrow() }

                subscription.await().toSubscription(plans.await())
            }
        }
    }

    suspend fun getPendingRequest(accountId: Int): Resource<SubscriptionRequest?> = Resource.Success(null)

    suspend fun getAllRequests(accountId: Int): Resource<List<SubscriptionRequest>> = Resource.Success(emptyList())

    suspend fun createRequest(request: SubscriptionRequest): Resource<Subscription> = withContext(Dispatchers.IO) {
        safeCall {
            if (request.planId == 0) {
                throw DomainException(PLAN_REQUIRED)
            }

            coroutineScope {
                val plans = async { fetchPlans() }
                val response = async {
                    service.changePlan(request.accountId, ChangePlanRequest(request.planId)).bodyOrThrow()
                }

                response.await().toSubscription(plans.await())
            }
        }
    }

    suspend fun cancelRequest(requestId: Int): Resource<SubscriptionRequest?> = Resource.Success(null)

    suspend fun approveRequest(request: SubscriptionRequest): Resource<Subscription> = createRequest(request)
}
