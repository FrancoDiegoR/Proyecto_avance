package com.smartgas_frontend.iam.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.iam.data.remote.SettingsService
import com.smartgas_frontend.iam.data.remote.dto.UpdateEmergencyContactRequest
import com.smartgas_frontend.iam.data.remote.dto.toAccountSettings
import com.smartgas_frontend.iam.data.remote.dto.toEmergencyContact
import com.smartgas_frontend.iam.data.remote.dto.toRequest
import com.smartgas_frontend.iam.domain.model.AccountSettings
import com.smartgas_frontend.iam.domain.model.EmergencyContact
import com.smartgas_frontend.kitchenmonitoring.data.remote.SensorService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toSensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.data.remote.MonitoringService
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.toZone
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository(
    private val service: SettingsService,
    private val monitoringService: MonitoringService,
    private val sensorService: SensorService
) {

    suspend fun getSettings(accountId: Int): Resource<AccountSettings> = withContext(Dispatchers.IO) {
        safeCall { service.getSettings(accountId).bodyOrThrow().toAccountSettings() }
    }

    suspend fun saveSettings(accountId: Int, data: AccountSettings): Resource<AccountSettings> =
        withContext(Dispatchers.IO) {
            safeCall { service.saveSettings(accountId, data.toRequest()).bodyOrThrow().toAccountSettings() }
        }

    suspend fun getEmergencyContact(accountId: Int): Resource<EmergencyContact> = withContext(Dispatchers.IO) {
        safeCall { service.getEmergencyContact(accountId).bodyOrThrow().toEmergencyContact(accountId) }
    }

    suspend fun saveEmergencyContact(accountId: Int, data: EmergencyContact): Resource<EmergencyContact> =
        withContext(Dispatchers.IO) {
            safeCall {
                val response = service.saveEmergencyContact(
                    accountId,
                    UpdateEmergencyContactRequest(name = data.name, phone = data.phone, email = data.email)
                )

                response.bodyOrThrow().toEmergencyContact(accountId)
            }
        }

    suspend fun getZones(accountId: Int): Resource<List<Zone>> = withContext(Dispatchers.IO) {
        safeCall { monitoringService.getZones(accountId).bodyOrThrow().map { it.toZone() } }
    }

    suspend fun getSensors(accountId: Int): Resource<List<Sensor>> = withContext(Dispatchers.IO) {
        safeCall { sensorService.getSensors(accountId).bodyOrThrow().map { it.toSensor() } }
    }
}
