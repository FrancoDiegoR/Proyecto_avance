package com.smartgas_frontend.shared.data.remote.dto

import com.smartgas_frontend.shared.domain.model.Weather

data class WeatherDto(
    val temperature: Double?,
    val temperatureUnit: String?,
    val relativeHumidity: Double?,
    val relativeHumidityUnit: String?,
    val windSpeed: Double?,
    val windSpeedUnit: String?,
    val source: String?
)

fun WeatherDto.toWeather() = Weather(
    temperature = temperature,
    temperatureUnit = temperatureUnit?.takeIf { it.isNotBlank() } ?: "°C",
    relativeHumidity = relativeHumidity,
    relativeHumidityUnit = relativeHumidityUnit?.takeIf { it.isNotBlank() } ?: "%",
    windSpeed = windSpeed,
    windSpeedUnit = windSpeedUnit?.takeIf { it.isNotBlank() } ?: "km/h",
    source = source?.takeIf { it.isNotBlank() } ?: "Open-Meteo"
)
