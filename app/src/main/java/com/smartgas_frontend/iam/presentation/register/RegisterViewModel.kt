package com.smartgas_frontend.iam.presentation.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.iam.data.repository.AccountRepository
import com.smartgas_frontend.iam.data.repository.EMAIL_EXISTS
import com.smartgas_frontend.iam.data.repository.RegisterProfileData
import com.smartgas_frontend.iam.presentation.login.emailRegex
import kotlinx.coroutines.launch

const val ACCOUNT_HOME = "domestic"
const val ACCOUNT_RESTAURANT = "commercial"

/** Distritos de Lima para el desplegable "District" del diseno 03. */
val limaDistricts = listOf(
    "Barranco", "Breña", "Callao", "Chorrillos", "Jesús María", "La Molina", "La Victoria",
    "Lima (Cercado)", "Lince", "Los Olivos", "Magdalena del Mar", "Miraflores", "Pueblo Libre",
    "San Borja", "San Isidro", "San Juan de Lurigancho", "San Juan de Miraflores", "San Miguel",
    "Santiago de Surco", "Surquillo", "Villa El Salvador", "Otro"
)

/** Regla del diseno: al menos 8 caracteres, un numero y una mayuscula. */
fun isValidPassword(password: String): Boolean =
    password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() }

data class RegisterForm(
    val accountType: String = ACCOUNT_RESTAURANT,
    val fullName: String = "",
    val businessName: String = "",
    val email: String = "",
    val password: String = "",
    val district: String = "",
    val acceptedTerms: Boolean = false
) {
    val isRestaurant: Boolean get() = accountType == ACCOUNT_RESTAURANT
}

class RegisterViewModel(private val repository: AccountRepository) : ViewModel() {

    private val _form = mutableStateOf(RegisterForm())
    val form: State<RegisterForm> get() = _form

    // Id del texto de error (strings.xml) para traducirlo en la pantalla.
    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> get() = _error

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> get() = _loading

    fun onFormChanged(form: RegisterForm) {
        _form.value = form
    }

    /** Registra la cuenta; [onSuccess] se llama si el backend la creo (la sesion ya queda guardada). */
    fun register(onSuccess: () -> Unit) {
        val form = _form.value

        _error.value = null

        // El nombre del negocio solo es obligatorio para restaurantes
        if (form.fullName.isBlank() || form.email.isBlank() || form.password.isBlank() ||
            (form.isRestaurant && form.businessName.isBlank())
        ) {
            _error.value = R.string.emptyFields
            return
        }

        if (!emailRegex.matches(form.email.trim())) {
            _error.value = R.string.emailInvalid
            return
        }

        if (!isValidPassword(form.password)) {
            _error.value = R.string.passwordRule
            return
        }

        if (form.district.isBlank()) {
            _error.value = R.string.districtRequired
            return
        }

        if (!form.acceptedTerms) {
            _error.value = R.string.acceptTerms
            return
        }

        _loading.value = true

        viewModelScope.launch {
            val result = repository.register(
                form.email.trim(),
                form.password,
                RegisterProfileData(
                    fullName = form.fullName.trim(),
                    accountType = form.accountType,
                    businessName = form.businessName.trim(),
                    district = form.district
                )
            )

            if (result is Resource.Success) {
                onSuccess()
            } else {
                _error.value = if (result.message == EMAIL_EXISTS) R.string.emailExists else R.string.apiError
            }

            _loading.value = false
        }
    }
}
