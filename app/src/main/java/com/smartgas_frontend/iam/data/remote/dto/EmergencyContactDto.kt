package com.smartgas_frontend.iam.data.remote.dto

import com.smartgas_frontend.iam.domain.model.EmergencyContact

data class EmergencyContactDto(
    val id: Int?,
    val accountId: Int?,
    val name: String?,
    val phone: String?,
    val email: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class UpdateEmergencyContactRequest(
    val name: String,
    val phone: String,
    val email: String
)

fun EmergencyContactDto?.toEmergencyContact(accountId: Int) = EmergencyContact(
    id = this?.id ?: 0,
    accountId = this?.accountId ?: accountId,
    name = this?.name ?: "",
    phone = this?.phone ?: "",
    email = this?.email ?: "",
    createdAt = this?.createdAt,
    updatedAt = this?.updatedAt
)
