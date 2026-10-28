package com.smartgas_frontend.shared.data.remote

import com.smartgas_frontend.shared.data.remote.dto.WeatherDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExternalWeatherService {
    @GET("external/weather/current")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<WeatherDto>
}
