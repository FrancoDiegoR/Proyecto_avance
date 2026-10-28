package com.smartgas_frontend.paymentmanagement.data.di

import com.smartgas_frontend.paymentmanagement.data.remote.SubscriptionService
import com.smartgas_frontend.paymentmanagement.data.repository.SubscriptionRepository
import com.smartgas_frontend.shared.data.di.SharedDataModule

object PaymentManagementDataModule {

    fun getSubscriptionService(): SubscriptionService {
        return SharedDataModule.getRetrofit().create(SubscriptionService::class.java)
    }

    fun getSubscriptionRepository(): SubscriptionRepository {
        return SubscriptionRepository(getSubscriptionService())
    }
}
