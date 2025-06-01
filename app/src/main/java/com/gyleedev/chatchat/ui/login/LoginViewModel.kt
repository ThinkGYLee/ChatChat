package com.gyleedev.chatchat.ui.login

import androidx.lifecycle.viewModelScope
import com.gyleedev.domain.model.LogInState
import com.gyleedev.domain.usecase.LoginProcessUseCase
import com.gyleedev.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val useCase: LoginProcessUseCase
) : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")

    private val _passwordQuery = MutableStateFlow("")

    private val _idIsAvailable = MutableStateFlow(false)
    val idIsAvailable: StateFlow<Boolean> = _idIsAvailable
    private val _passwordIsAvailable = MutableStateFlow(false)
    val passwordIsAvailable: StateFlow<Boolean> = _passwordIsAvailable

    private val _logInIsAvailable = MutableStateFlow(false)
    val logInIsAvailable: StateFlow<Boolean> = _logInIsAvailable

    private val _logInResult = MutableSharedFlow<LogInState>()
    val logInResult: SharedFlow<LogInState> = _logInResult

    fun editId(id: String) {
        viewModelScope.launch {
            _idQuery.emit(id)
            checkSignInAvailable()
        }
    }

    fun editPassword(password: String) {
        viewModelScope.launch {
            _passwordQuery.emit(password)
            checkSignInAvailable()
        }
    }

    private suspend fun checkSignInAvailable() {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        _idIsAvailable.emit(pattern.matcher(_idQuery.value).matches())
        _passwordIsAvailable.emit(_passwordQuery.value.length >= 8)
        _logInIsAvailable.emit(_idIsAvailable.value && _passwordIsAvailable.value)
    }

    fun logInButtonClick() {
        viewModelScope.launch {
            try {
                _logInResult.emit(useCase(_idQuery.value, _passwordQuery.value))
            } catch (e: Exception) {
                _logInResult.emit(LogInState.Failure(e.message.toString()))
            }
        }
    }
}
