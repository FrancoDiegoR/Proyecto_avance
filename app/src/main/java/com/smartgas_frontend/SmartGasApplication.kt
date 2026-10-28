package com.smartgas_frontend

import android.app.Application

/**
 * Igual que MealsApplication: guarda la instancia para que los DataModule
 * puedan pedir el applicationContext (SharedPreferences de sesion y preferencias).
 */
class SmartGasApplication : Application() {

    companion object {
        lateinit var instance: SmartGasApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
