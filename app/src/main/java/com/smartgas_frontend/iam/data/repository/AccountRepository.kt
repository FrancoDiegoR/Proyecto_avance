package com.smartgas_frontend.iam.data.repository

import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.iam.data.remote.AuthService
import com.smartgas_frontend.iam.data.remote.dto.SignInRequest
import com.smartgas_frontend.iam.data.remote.dto.SignUpRequest
import com.smartgas_frontend.iam.data.remote.dto.toAccount
import com.smartgas_frontend.iam.domain.model.Account
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.remote.DomainException
import com.smartgas_frontend.shared.data.remote.bodyOrThrow
import com.smartgas_frontend.shared.data.remote.errorMessage
import com.smartgas_frontend.shared.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
const val EMAIL_EXISTS = "EMAIL_EXISTS"

data class RegisterProfileData(
    val fullName: String = "",
    val accountType: String = "commercial",
    val businessName: String = "",
    val phone: String = "",
    val district: String = ""
)

class AccountRepository(
    private val service: AuthService,
    private val sessionService: SessionService
) {

    suspend fun login(email: String, password: String, remember: Boolean = true): Resource<Account> = withContext(Dispatchers.IO) {
        safeCall {
            val response = service.signIn(SignInRequest(email, password))

            if (response.code() == 400 || response.code() == 401 || response.code() == 404) {
                throw DomainException(INVALID_CREDENTIALS)
            }

            val account = response.bodyOrThrow().toAccount()
            sessionService.save(account, persist = remember)
            account
        }
    }

    suspend fun register(
        email: String,
        password: String,
        profileData: RegisterProfileData = RegisterProfileData()
    ): Resource<Account> = withContext(Dispatchers.IO) {
        safeCall {
            val accountType = profileData.accountType.ifBlank { "commercial" }

            val response = service.signUp(
                SignUpRequest(
                    email = email,
                    password = password,
                    fullName = profileData.fullName.ifBlank { email.substringBefore('@') },
                    businessName = profileData.businessName.ifBlank { "SmartGas monitored facility" },
                    phone = profileData.phone,
                    district = profileData.district,
                    role = if (accountType == "commercial") "RestaurantAdministrator" else "HomeOwner"
                )
            )

            if (!response.isSuccessful) {
                val message = response.errorMessage().lowercase()

                if (response.code() == 409 || message.contains("email") || message.contains("already")) {
                    throw DomainException(EMAIL_EXISTS)
                }
            }

            val account = response.bodyOrThrow().toAccount()
            sessionService.save(account)
            account
        }
    }
}
