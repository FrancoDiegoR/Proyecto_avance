package com.smartgas_frontend.iam.data.remote.dto

import com.smartgas_frontend.iam.domain.model.Account
import com.smartgas_frontend.iam.domain.model.Profile
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription

data class ProfileDto(
    val id: Int?,
    val accountId: Int?,
    val fullName: String?,
    val email: String?,
    val role: String?,
    val accountType: String?,
    val businessName: String?,
    val phone: String?,
    val district: String?,
    val address: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class UpdateProfileRequest(
    val fullName: String,
    val businessName: String,
    val phone: String,
    val district: String,
    val address: String
)

fun roleToAccountType(role: String?): String {
    val normalizedRole = role.orEmpty().lowercase()

    return when {
        normalizedRole.contains("restaurant") ||
            normalizedRole.contains("commercial") ||
            normalizedRole.contains("administrator") -> "commercial"
        normalizedRole.contains("home") || normalizedRole.contains("domestic") -> "domestic"
        else -> ""
    }
}

fun ProfileDto.toProfile(session: Account?, subscription: Subscription? = null): Profile {
    val resolvedRole = role?.takeIf { it.isNotBlank() } ?: session?.role ?: ""

    return Profile(
        id = accountId ?: session?.id ?: 0,
        profileId = id,
        fullName = fullName ?: "",
        email = email?.takeIf { it.isNotBlank() } ?: session?.email ?: "",
        role = resolvedRole,
        accountType = accountType?.takeIf { it.isNotBlank() }
            ?: session?.accountType?.takeIf { it.isNotBlank() }
            ?: roleToAccountType(resolvedRole),
        businessName = businessName ?: "",
        phone = phone ?: "",
        district = district ?: "",
        address = address ?: "",
        memberSince = createdAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        planId = subscription?.planId,
        planName = subscription?.planName ?: ""
    )
}
