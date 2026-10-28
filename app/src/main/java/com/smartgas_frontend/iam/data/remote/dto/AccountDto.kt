package com.smartgas_frontend.iam.data.remote.dto

import com.smartgas_frontend.iam.domain.model.Account

data class AccountDto(
    val accountId: Int?,
    val id: Int?,
    val email: String?,
    val fullName: String?,
    val name: String?,
    val businessName: String?,
    val role: String?,
    val status: String?,
    val token: String?,
    val accessToken: String?,
    val account: AccountRefDto?,
    val profile: ProfileRefDto?
)

data class AccountRefDto(
    val id: Int?,
    val email: String?,
    val role: String?,
    val status: String?
)

data class ProfileRefDto(
    val fullName: String?,
    val businessName: String?
)

fun AccountDto.toAccount(): Account {
    val resolvedEmail = email ?: account?.email ?: ""
    val resolvedFullName = fullName ?: name ?: profile?.fullName

    return Account(
        id = accountId ?: id ?: account?.id ?: 0,
        email = resolvedEmail,
        name = resolvedFullName ?: resolvedEmail,
        fullName = resolvedFullName ?: "",
        businessName = businessName ?: profile?.businessName ?: "",
        role = role ?: account?.role ?: "",
        status = status ?: account?.status ?: "Active",
        token = token ?: accessToken
    )
}
