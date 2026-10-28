package com.smartgas_frontend.kitchenmonitoring.data.remote

import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.CreateSensorReadingRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.CreateZoneRequest
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.ReadingResultDto
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.SensorReadingDto
import com.smartgas_frontend.kitchenmonitoring.data.remote.dto.ZoneDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MonitoringService {
    @GET("zones")
    suspend fun getZones(@Query("accountId") accountId: Int): Response<List<ZoneDto>>

    @POST("zones")
    suspend fun createZone(@Body request: CreateZoneRequest): Response<ZoneDto>

    @GET("sensor-readings")
    suspend fun getSensorReadings(@Query("accountId") accountId: Int): Response<List<SensorReadingDto>>

    @POST("sensor-readings")
    suspend fun createSensorReading(@Body request: CreateSensorReadingRequest): Response<ReadingResultDto>
}
