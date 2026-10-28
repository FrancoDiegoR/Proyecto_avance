package com.smartgas_frontend.iam.domain.model

data class Account(
    val id: Int,
    val email: String,
    val name: String,
    val fullName: String,
    val businessName: String,
    val role: String,
    val status: String,
    val token: String?,
    val accountType: String = ""
)
