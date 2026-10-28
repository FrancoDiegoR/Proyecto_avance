package com.smartgas_frontend.iam.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.iam.data.repository.AccountRepository
import com.smartgas_frontend.iam.data.repository.INVALID_CREDENTIALS
import kotlinx.coroutines.launch

val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

class LoginViewModel(private val repository: AccountRepository) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> get() = _email

    private val _password = mutableStateOf("")
    val password: State<String> get() = _password

    private val _remember = mutableStateOf(true)
    val remember: State<Boolean> get() = _remember

    // Id del texto de error (strings.xml) para traducirlo en la pantalla.
    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> get() = _error

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> get() = _loading

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onRememberChanged(remember: Boolean) {
        _remember.value = remember
    }

    fun login(onSuccess: () -> Unit) {
        _error.value = null

        if (_email.value.isEmpty() || _password.value.isEmpty()) {
            _error.value = R.string.emptyFields
            return
        }

        if (!emailRegex.matches(_email.value)) {
            _error.value = R.string.emailInvalid
            return
        }

        _loading.value = true

        viewModelScope.launch {
            val result = repository.login(_email.value, _password.value, _remember.value)

            if (result is Resource.Success) {
                onSuccess()
            } else {
                _error.value =
                    if (result.message == INVALID_CREDENTIALS) R.string.invalidCredentials else R.string.apiError
            }

            _loading.value = false
        }
    }
}
