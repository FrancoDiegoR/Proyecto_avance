package com.smartgas_frontend.iam.data.remote.dto

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val businessName: String,
    val phone: String,
    val district: String,
    val role: String
)
