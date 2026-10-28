package com.smartgas_frontend.iam.presentation.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.iam.data.repository.SettingsRepository
import com.smartgas_frontend.iam.domain.model.AccountSettings
import com.smartgas_frontend.iam.domain.model.EmergencyContact
import com.smartgas_frontend.iam.domain.model.NotificationPreferences
import com.smartgas_frontend.iam.domain.model.SafetySettings
import com.smartgas_frontend.kitchenmonitoring.domain.model.Sensor
import com.smartgas_frontend.kitchenmonitoring.domain.model.Zone
import com.smartgas_frontend.shared.data.local.AppPreferences
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.data.local.normalizeLocale
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.res
import com.smartgas_frontend.shared.utils.formatNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class SettingsForm(
    val gasThreshold: String = "50",
    val temperatureThreshold: String = "45",
    val notificationsEnabled: Boolean = true,
    val language: String = "en",
    val darkMode: Boolean = false
)

data class SettingsData(
    val zones: List<Zone> = emptyList(),
    val sensors: List<Sensor> = emptyList()
) {
    fun sensorCount(zoneId: Int): Int = sensors.count { it.zoneId == zoneId }
}

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val sessionService: SessionService,
    private val preferences: AppPreferences
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<SettingsData>(isLoading = true))
    val state: State<UIState<SettingsData>> get() = _state

    private val _form = mutableStateOf(SettingsForm(language = preferences.language, darkMode = preferences.darkMode))
    val form: State<SettingsForm> get() = _form

    private val _emergencyContact = mutableStateOf(EmergencyContact())
    val emergencyContact: State<EmergencyContact> get() = _emergencyContact

    private val _saveLoading = mutableStateOf(false)
    val saveLoading: State<Boolean> get() = _saveLoading

    private var originalForm = _form.value
    private var originalEmergencyContact = _emergencyContact.value

    init {
        loadData()
    }

    private fun applyVisualPreferences() {
        preferences.setDarkTheme(_form.value.darkMode)
    }

    private fun applyLanguage(language: String) {
        val normalizedLanguage = preferences.setSmartGasLocale(normalizeLocale(language))
        _form.value = _form.value.copy(language = normalizedLanguage)
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val settings = async { repository.getSettings(accountId) }
            val contact = async { repository.getEmergencyContact(accountId) }
            val zones = async { repository.getZones(accountId) }
            val sensors = async { repository.getSensors(accountId) }

            val settingsResult = settings.await()
            val contactResult = contact.await()
            val zonesResult = zones.await()
            val sensorsResult = sensors.await()

            val loadedSettings = settingsResult.data
            val loadedContact = contactResult.data

            if (settingsResult is Resource.Success && contactResult is Resource.Success &&
                zonesResult is Resource.Success && sensorsResult is Resource.Success &&
                loadedSettings != null && loadedContact != null
            ) {
                // Idioma y tema se leen de las preferencias locales, igual que la web usa localStorage.
                _form.value = SettingsForm(
                    gasThreshold = formatNumber(loadedSettings.safety.gasThreshold),
                    temperatureThreshold = formatNumber(loadedSettings.safety.temperatureThreshold),
                    notificationsEnabled = loadedSettings.notifications.notificationsEnabled,
                    language = preferences.language,
                    darkMode = preferences.darkMode
                )
                _emergencyContact.value = loadedContact

                originalForm = _form.value
                originalEmergencyContact = _emergencyContact.value

                _state.value = UIState(
                    data = SettingsData(zones = zonesResult.data.orEmpty(), sensors = sensorsResult.data.orEmpty())
                )
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }

    fun onFormChanged(form: SettingsForm) {
        val previous = _form.value
        _form.value = form

        if (form.darkMode != previous.darkMode) applyVisualPreferences()
        if (form.language != previous.language) applyLanguage(form.language)
    }

    fun onEmergencyContactChanged(contact: EmergencyContact) {
        _emergencyContact.value = contact
    }

    private fun validateSettings(): Boolean {
        val form = _form.value
        val contact = _emergencyContact.value

        val valid = (form.gasThreshold.toDoubleOrNull() ?: 0.0) > 0 &&
            (form.temperatureThreshold.toDoubleOrNull() ?: 0.0) > 0 &&
            contact.name.isNotBlank() &&
            contact.phone.isNotBlank() &&
            contact.email.isNotBlank()

        if (!valid) {
            toast(ToastSeverity.Warning, res(R.string.emptyFields))
        }

        return valid
    }

    fun saveAll() {
        if (!validateSettings()) return

        _saveLoading.value = true

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()
            val form = _form.value
            val contact = _emergencyContact.value

            val settings = async {
                repository.saveSettings(
                    accountId,
                    AccountSettings(
                        safety = SafetySettings(
                            gasThreshold = form.gasThreshold.toDouble(),
                            temperatureThreshold = form.temperatureThreshold.toDouble()
                        ),
                        notifications = NotificationPreferences(form.notificationsEnabled),
                        language = form.language,
                        darkMode = form.darkMode
                    )
                )
            }
            val emergency = async {
                repository.saveEmergencyContact(
                    accountId,
                    contact.copy(name = contact.name.trim(), phone = contact.phone.trim(), email = contact.email.trim())
                )
            }

            val settingsResult = settings.await()
            val emergencyResult = emergency.await()

            val savedSettings = settingsResult.data
            val savedContact = emergencyResult.data

            if (settingsResult is Resource.Success && emergencyResult is Resource.Success &&
                savedSettings != null && savedContact != null
            ) {
                _form.value = form.copy(
                    gasThreshold = formatNumber(savedSettings.safety.gasThreshold),
                    temperatureThreshold = formatNumber(savedSettings.safety.temperatureThreshold),
                    notificationsEnabled = savedSettings.notifications.notificationsEnabled,
                    language = normalizeLocale(form.language)
                )
                _emergencyContact.value = savedContact

                originalForm = _form.value
                originalEmergencyContact = _emergencyContact.value

                applyLanguage(_form.value.language)
                applyVisualPreferences()

                toast(ToastSeverity.Success, res(R.string.settingsSaved))
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }

            _saveLoading.value = false
        }
    }

    fun resetAll() {
        _form.value = originalForm
        _emergencyContact.value = originalEmergencyContact

        applyLanguage(_form.value.language)
        applyVisualPreferences()

        toast(ToastSeverity.Info, res(R.string.resetApplied))
    }
}
