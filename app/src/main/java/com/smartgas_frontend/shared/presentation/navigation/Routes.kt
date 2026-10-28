package com.smartgas_frontend.shared.presentation.navigation

/** Rutas de la app movil, segun el diseno (barra inferior: Home, Zones, Alerts, History, Settings). */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val APP = "app"

    // Pestanas de la barra inferior
    const val HOME = "app/home"
    const val ZONES = "app/zones"
    const val ALERTS = "app/alerts"
    const val HISTORY = "app/history"
    const val SETTINGS = "app/settings"

    // Pantallas secundarias (sin pestana propia)
    const val ZONE_ID_ARG = "zoneId"
    const val ZONE_DETAIL = "app/zones/{$ZONE_ID_ARG}"   // se abre al tocar una zona en Zones
    fun zoneDetail(zoneId: Int) = "app/zones/$zoneId"

    // Cuelgan de app/zones/ para que la pestana Zones quede marcada (como en el diseno 07)
    const val SENSOR_ID_ARG = "sensorId"
    const val SENSOR_DETAIL = "app/zones/sensor/{$SENSOR_ID_ARG}"
    fun sensorDetail(sensorId: Int) = "app/zones/sensor/$sensorId"
    const val EDIT_SENSOR = "app/zones/sensor/{$SENSOR_ID_ARG}/edit"
    fun editSensor(sensorId: Int) = "app/zones/sensor/$sensorId/edit"

    // zoneId es opcional: desde Zone detail llega preseleccionada, desde el + de Zones no
    const val ADD_SENSOR = "app/zones/new/sensor?$ZONE_ID_ARG={$ZONE_ID_ARG}"
    fun addSensor(zoneId: Int? = null) =
        if (zoneId != null) "app/zones/new/sensor?$ZONE_ID_ARG=$zoneId" else "app/zones/new/sensor"

    const val SENSORS = "app/sensors"     // lista de sensores (desde Settings)
    const val PROFILE = "app/profile"     // se abre con el icono de usuario de Home
    const val PLANS = "app/plans"
    const val REPORTS = "app/reports"
    const val NOT_FOUND = "not-found"
}
