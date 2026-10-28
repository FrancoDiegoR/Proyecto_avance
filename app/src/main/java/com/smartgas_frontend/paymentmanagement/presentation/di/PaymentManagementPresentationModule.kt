package com.smartgas_frontend.paymentmanagement.presentation.di

import com.smartgas_frontend.paymentmanagement.data.di.PaymentManagementDataModule
import com.smartgas_frontend.paymentmanagement.presentation.plans.PlansViewModel
import com.smartgas_frontend.shared.data.di.SharedDataModule

object PaymentManagementPresentationModule {

    fun getPlansViewModel(): PlansViewModel {
        return PlansViewModel(PaymentManagementDataModule.getSubscriptionRepository(), SharedDataModule.getSessionService())
    }
}
