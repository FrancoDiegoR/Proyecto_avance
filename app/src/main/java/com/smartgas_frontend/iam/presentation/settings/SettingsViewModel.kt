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
import com.smartgas_frontend.paymentmanagement.data.repository.SubscriptionRepository
import com.smartgas_frontend.shared.data.local.AppPreferences
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.UiText
import com.smartgas_frontend.shared.presentation.components.res
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Todo lo que muestra la pantalla de Ajustes (diseno 15). */
data class SettingsData(
    val gasThreshold: Double = 50.0,
    val temperatureThreshold: Double = 45.0,
    val notificationsEnabled: Boolean = true,
    val contact: EmergencyContact = EmergencyContact(),
    val planName: String = ""
)

private fun text(en: String, es: String): UiText = { if (it.isSpanish) es else en }

/**
 * Diseno 15: cada cambio se guarda al momento (no hay boton "Guardar"):
 * - umbrales al soltar el slider, interruptores al tocarlos, contacto al cerrar su dialogo.
 * Idioma, tema, push y vibracion tambien se guardan en el telefono (AppPreferences).
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val sessionService: SessionService,
    val preferences: AppPreferences
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState(isLoading = true, data = SettingsData()))
    val state: State<UIState<SettingsData>> get() = _state

    private fun current(): SettingsData = _state.value.data ?: SettingsData()

    private fun update(data: SettingsData) {
        _state.value = UIState(isLoading = _state.value.isLoading, data = data, message = _state.value.message)
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = current())

        viewModelScope.launch {
            val accountId = sessionService.getAccountId()

            val settings = async { repository.getSettings(accountId) }
            val contact = async { repository.getEmergencyContact(accountId) }
            val subscription = async { subscriptionRepository.getSubscription(accountId) }

            val settingsResult = settings.await()
            val loaded = settingsResult.data

            if (settingsResult is Resource.Success && loaded != null) {
                _state.value = UIState(
                    data = SettingsData(
                        gasThreshold = loaded.safety.gasThreshold,
                        temperatureThreshold = loaded.safety.temperatureThreshold,
                        notificationsEnabled = loaded.notifications.notificationsEnabled,
                        contact = contact.await().data ?: EmergencyContact(),
                        planName = subscription.await().data?.planName.orEmpty()
                    )
                )
            } else {
                _state.value = UIState(data = current(), message = settingsResult.message ?: "An error occurred")
            }
        }
    }

    // --- Umbrales: el slider cambia el valor en pantalla y se guarda al soltarlo ---

    fun onGasThresholdChanged(value: Float) = update(current().copy(gasThreshold = value.roundToInt().toDouble()))

    fun onTemperatureThresholdChanged(value: Float) =
        update(current().copy(temperatureThreshold = value.roundToInt().toDouble()))

    fun saveThresholds() = saveSettings()

    // --- Interruptores ---

    fun setPushEnabled(enabled: Boolean) {
        preferences.updatePushEnabled(enabled)
        update(current().copy(notificationsEnabled = enabled))
        saveSettings()
    }

    fun setVibrateOnCritical(enabled: Boolean) {
        // Solo afecta a este telefono: no se envia al backend
        preferences.updateVibrateOnCritical(enabled)
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.setDarkTheme(enabled)
        saveSettings()
    }

    fun setLanguage(language: String) {
        preferences.setSmartGasLocale(language)
        saveSettings()
    }

    private fun saveSettings() {
        val data = current()

        viewModelScope.launch {
            val result = repository.saveSettings(
                sessionService.getAccountId(),
                AccountSettings(
                    safety = SafetySettings(
                        gasThreshold = data.gasThreshold,
                        temperatureThreshold = data.temperatureThreshold
                    ),
                    notifications = NotificationPreferences(data.notificationsEnabled),
                    language = preferences.language,
                    darkMode = preferences.darkMode
                )
            )

            if (result is Resource.Success) {
                toast(ToastSeverity.Success, res(R.string.settingsSaved))
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }
        }
    }

    // --- Contacto de emergencia (dialogo) ---

    /** Devuelve false (y no guarda) si falta algun dato; [onSaved] se llama si el backend lo acepto. */
    fun saveEmergencyContact(contact: EmergencyContact, onSaved: () -> Unit): Boolean {
        val clean = contact.copy(name = contact.name.trim(), phone = contact.phone.trim(), email = contact.email.trim())

        if (clean.name.isBlank() || clean.phone.isBlank() || clean.email.isBlank()) {
            toast(ToastSeverity.Warning, res(R.string.emptyFields))
            return false
        }

        viewModelScope.launch {
            val result = repository.saveEmergencyContact(sessionService.getAccountId(), clean)
            val saved = result.data

            if (result is Resource.Success && saved != null) {
                update(current().copy(contact = saved))
                toast(ToastSeverity.Success, text("Emergency contact saved.", "Contacto de emergencia guardado."))
                onSaved()
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }
        }
        return true
    }
}
