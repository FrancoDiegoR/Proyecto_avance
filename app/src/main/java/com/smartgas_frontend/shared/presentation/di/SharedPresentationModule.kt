package com.smartgas_frontend.shared.presentation.di

import com.smartgas_frontend.shared.data.di.SharedDataModule
import com.smartgas_frontend.shared.presentation.home.HomeViewModel

object SharedPresentationModule {

    fun getHomeViewModel(): HomeViewModel {
        return HomeViewModel(SharedDataModule.getHomeRepository(), SharedDataModule.getSessionService())
    }
}
