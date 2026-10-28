package com.smartgas_frontend.kitchenmonitoring.data.remote

import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.CreateSensorRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.SensorDto
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.UpdateSensorRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SensorService {
    @GET("sensors")
    suspend fun getSensors(@Query("accountId") accountId: Int): Response<List<SensorDto>>

    @POST("sensors")
    suspend fun createSensor(@Body request: CreateSensorRequest): Response<SensorDto>

    @PATCH("sensors/{id}")
    suspend fun updateSensor(@Path("id") id: Int, @Body request: UpdateSensorRequest): Response<SensorDto>
}
