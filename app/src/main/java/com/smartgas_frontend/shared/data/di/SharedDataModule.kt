package com.smartgas_frontend.shared.data.di

import com.smartgas_frontend.SmartGasApplication
import com.smartgas_frontend.common.Constants.BASE_URL
import com.smartgas_frontend.incidentdetection.data.di.IncidentDetectionDataModule
import com.smartgas_frontend.incidentpreventionnotification.data.di.IncidentPreventionNotificationDataModule
import com.smartgas_frontend.kitchenmonitoring.data.di.KitchenMonitoringDataModule
import com.smartgas_frontend.paymentmanagement.data.di.PaymentManagementDataModule
import com.smartgas_frontend.shared.data.local.AppPreferences
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.remote.ExternalWeatherService
import com.smartgas_frontend.shared.data.repository.HomeRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Mismo patron que DataModule de MealsCompose.
 * Retrofit, la sesion y las preferencias se crean una sola vez (by lazy) porque
 * AppPreferences guarda el idioma y el tema como estado de Compose y toda la app
 * debe leer la misma instancia.
 */
object SharedDataModule {

    private val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val sessionServiceInstance: SessionService by lazy {
        SessionService(SmartGasApplication.instance.applicationContext)
    }

    private val appPreferencesInstance: AppPreferences by lazy {
        AppPreferences(SmartGasApplication.instance.applicationContext)
    }

    fun getRetrofit(): Retrofit = retrofitInstance

    fun getSessionService(): SessionService = sessionServiceInstance

    fun getAppPreferences(): AppPreferences = appPreferencesInstance

    fun getExternalWeatherService(): ExternalWeatherService {
        return getRetrofit().create(ExternalWeatherService::class.java)
    }

    fun getHomeRepository(): HomeRepository {
        return HomeRepository(
            KitchenMonitoringDataModule.getSensorService(),
            KitchenMonitoringDataModule.getMonitoringService(),
            IncidentDetectionDataModule.getIncidentService(),
            IncidentPreventionNotificationDataModule.getAlertService(),
            PaymentManagementDataModule.getSubscriptionService(),
            getExternalWeatherService()
        )
    }
}
