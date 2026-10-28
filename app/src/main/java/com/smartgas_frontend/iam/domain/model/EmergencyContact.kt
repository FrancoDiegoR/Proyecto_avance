package com.smartgas_frontend.iam.domain.model

data class EmergencyContact(
    val id: Int = 0,
    val accountId: Int = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
