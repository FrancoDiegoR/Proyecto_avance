package com.smartgas_frontend.kitchenmonitoring.presentation.di

import com.smartgas_frontend.kitchenmonitoring.data.di.KitchenMonitoringDataModule
import com.smartgas_frontend.kitchenmonitoring.presentation.addsensor.AddSensorViewModel
import com.smartgas_frontend.kitchenmonitoring.presentation.sensordetail.SensorDetailViewModel
import com.smartgas_frontend.kitchenmonitoring.presentation.sensors.SensorsViewModel
import com.smartgas_frontend.kitchenmonitoring.presentation.zonedetail.ZoneDetailViewModel
import com.smartgas_frontend.kitchenmonitoring.presentation.zones.ZonesViewModel
import com.smartgas_frontend.shared.data.di.SharedDataModule

object KitchenMonitoringPresentationModule {

    fun getZonesViewModel(): ZonesViewModel {
        return ZonesViewModel(KitchenMonitoringDataModule.getMonitoringRepository(), SharedDataModule.getSessionService())
    }

    fun getZoneDetailViewModel(zoneId: Int): ZoneDetailViewModel {
        return ZoneDetailViewModel(
            KitchenMonitoringDataModule.getMonitoringRepository(),
            SharedDataModule.getSessionService(),
            zoneId
        )
    }

    fun getSensorDetailViewModel(sensorId: Int): SensorDetailViewModel {
        return SensorDetailViewModel(
            KitchenMonitoringDataModule.getSensorRepository(),
            KitchenMonitoringDataModule.getMonitoringRepository(),
            SharedDataModule.getSessionService(),
            sensorId
        )
    }

    fun getAddSensorViewModel(zoneId: Int?): AddSensorViewModel {
        return AddSensorViewModel(
            KitchenMonitoringDataModule.getSensorRepository(),
            SharedDataModule.getSessionService(),
            zoneId
        )
    }

    fun getEditSensorViewModel(sensorId: Int): AddSensorViewModel {
        return AddSensorViewModel(
            KitchenMonitoringDataModule.getSensorRepository(),
            SharedDataModule.getSessionService(),
            initialZoneId = null,
            editingSensorId = sensorId
        )
    }

    fun getSensorsViewModel(): SensorsViewModel {
        return SensorsViewModel(KitchenMonitoringDataModule.getSensorRepository(), SharedDataModule.getSessionService())
    }
}
