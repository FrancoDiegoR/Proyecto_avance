package com.smartgas_frontend.iam.data.remote

import com.smartgas_frontend.iam.data.remote.dto.AccountDto
import com.smartgas_frontend.iam.data.remote.dto.SignInRequest
import com.smartgas_frontend.iam.data.remote.dto.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/sign-in")
    suspend fun signIn(@Body request: SignInRequest): Response<AccountDto>

    @POST("auth/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): Response<AccountDto>
}
