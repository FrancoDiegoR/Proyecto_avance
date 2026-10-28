package com.smartgas_frontend.iam.data.di

import com.smartgas_frontend.iam.data.remote.AuthService
import com.smartgas_frontend.iam.data.remote.ProfileService
import com.smartgas_frontend.iam.data.remote.SettingsService
import com.smartgas_frontend.iam.data.repository.AccountRepository
import com.smartgas_frontend.iam.data.repository.ProfileRepository
import com.smartgas_frontend.iam.data.repository.SettingsRepository
import com.smartgas_frontend.incidentdetection.data.di.IncidentDetectionDataModule
import com.smartgas_frontend.kitchenmonitoring.data.di.KitchenMonitoringDataModule
import com.smartgas_frontend.paymentmanagement.data.di.PaymentManagementDataModule
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IamDataModule {

    fun getAuthService(): AuthService {
        return SharedDataModule.getRetrofit().create(AuthService::class.java)
    }

    fun getProfileService(): ProfileService {
        return SharedDataModule.getRetrofit().create(ProfileService::class.java)
    }

    fun getSettingsService(): SettingsService {
        return SharedDataModule.getRetrofit().create(SettingsService::class.java)
    }

    fun getAccountRepository(): AccountRepository {
        return AccountRepository(getAuthService(), SharedDataModule.getSessionService())
    }

    fun getProfileRepository(): ProfileRepository {
        return ProfileRepository(
            getProfileService(),
            PaymentManagementDataModule.getSubscriptionService(),
            KitchenMonitoringDataModule.getSensorService(),
            KitchenMonitoringDataModule.getMonitoringService(),
            IncidentDetectionDataModule.getIncidentService(),
            SharedDataModule.getSessionService()
        )
    }

    fun getSettingsRepository(): SettingsRepository {
        return SettingsRepository(
            getSettingsService(),
            KitchenMonitoringDataModule.getMonitoringService(),
            KitchenMonitoringDataModule.getSensorService()
        )
    }
}
