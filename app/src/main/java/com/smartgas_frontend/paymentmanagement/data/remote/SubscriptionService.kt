package com.smartgas_frontend.paymentmanagement.data.remote

import com.smartgas_frontend.paymentmanagement.data.remote.dto.ChangePlanRequest
import com.smartgas_frontend.paymentmanagement.data.remote.dto.PlanDto
import com.smartgas_frontend.paymentmanagement.data.remote.dto.SubscriptionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface SubscriptionService {
    @GET("plans")
    suspend fun getPlans(): Response<List<PlanDto>>

    @GET("subscriptions/current/{accountId}")
    suspend fun getCurrentSubscription(@Path("accountId") accountId: Int): Response<SubscriptionDto>

    @PATCH("subscriptions/current/{accountId}/change-plan")
    suspend fun changePlan(
        @Path("accountId") accountId: Int,
        @Body request: ChangePlanRequest
    ): Response<SubscriptionDto>
}
