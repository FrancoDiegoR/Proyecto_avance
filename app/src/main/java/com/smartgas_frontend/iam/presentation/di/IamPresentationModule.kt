package com.smartgas_frontend.iam.presentation.di

import com.smartgas_frontend.iam.data.di.IamDataModule
import com.smartgas_frontend.iam.presentation.login.LoginViewModel
import com.smartgas_frontend.iam.presentation.profile.ProfileViewModel
import com.smartgas_frontend.iam.presentation.register.RegisterViewModel
import com.smartgas_frontend.iam.presentation.settings.SettingsViewModel
import com.smartgas_frontend.shared.data.di.SharedDataModule

object IamPresentationModule {

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(IamDataModule.getAccountRepository())
    }

    fun getRegisterViewModel(): RegisterViewModel {
        return RegisterViewModel(IamDataModule.getAccountRepository())
    }

    fun getProfileViewModel(): ProfileViewModel {
        return ProfileViewModel(IamDataModule.getProfileRepository(), SharedDataModule.getSessionService())
    }

    fun getSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            IamDataModule.getSettingsRepository(),
            SharedDataModule.getSessionService(),
            SharedDataModule.getAppPreferences()
        )
    }
}
