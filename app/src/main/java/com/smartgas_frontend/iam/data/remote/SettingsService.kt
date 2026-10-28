package com.smartgas_frontend.iam.data.remote

import com.smartgas_frontend.iam.data.remote.dto.EmergencyContactDto
import com.smartgas_frontend.iam.data.remote.dto.SettingsDto
import com.smartgas_frontend.iam.data.remote.dto.UpdateEmergencyContactRequest
import com.smartgas_frontend.iam.data.remote.dto.UpdateSettingsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface SettingsService {
    @GET("settings/{accountId}")
    suspend fun getSettings(@Path("accountId") accountId: Int): Response<SettingsDto>

    @PATCH("settings/{accountId}")
    suspend fun saveSettings(
        @Path("accountId") accountId: Int,
        @Body request: UpdateSettingsRequest
    ): Response<SettingsDto>

    @GET("emergency-contacts/{accountId}")
    suspend fun getEmergencyContact(@Path("accountId") accountId: Int): Response<EmergencyContactDto>

    @PATCH("emergency-contacts/{accountId}")
    suspend fun saveEmergencyContact(
        @Path("accountId") accountId: Int,
        @Body request: UpdateEmergencyContactRequest
    ): Response<EmergencyContactDto>
}
