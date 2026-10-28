package com.smartgas_frontend.iam.data.remote

import com.smartgas_frontend.iam.data.remote.dto.ProfileDto
import com.smartgas_frontend.iam.data.remote.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ProfileService {
    @GET("profiles/{accountId}")
    suspend fun getProfile(@Path("accountId") accountId: Int): Response<ProfileDto>

    @PATCH("profiles/{accountId}")
    suspend fun updateProfile(
        @Path("accountId") accountId: Int,
        @Body request: UpdateProfileRequest
    ): Response<ProfileDto>
}
