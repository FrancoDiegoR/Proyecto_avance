package com.smartgas_frontend.common

object Constants {
    // Backend local via "adb reverse tcp:5048 tcp:5048" (ver docs/mock/server.js).
    // Alternativa estandar del emulador: http://10.0.2.2:5048/api/v1/
    const val BASE_URL = "http://localhost:5048/api/v1/"

    const val DEFAULT_LATITUDE = -12.0464
    const val DEFAULT_LONGITUDE = -77.0428
}
