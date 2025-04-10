package com.gyleedev.chatchat.ui.login

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.LogInResult
import com.gyleedev.chatchat.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val useCase: LoginUseCase
) : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")
    private val idQuery: StateFlow<String> = _idQuery

    private val _passwordQuery = MutableStateFlow("")
    private val passwordQuery: StateFlow<String> = _passwordQuery

    private val _idIsAvailable = MutableStateFlow(false)
    val idIsAvailable: StateFlow<Boolean> = _idIsAvailable
    private val _passwordIsAvailable = MutableStateFlow(false)
    val passwordIsAvailable: StateFlow<Boolean> = _passwordIsAvailable

    private val _logInIsAvailable = MutableStateFlow(false)
    val logInIsAvailable: StateFlow<Boolean> = _logInIsAvailable

    private val _logInResult = MutableSharedFlow<LogInResult>()
    val logInResult: SharedFlow<LogInResult> = _logInResult

    fun editId(id: String) {
        viewModelScope.launch {
            _idQuery.emit(id)
            checkSignInAvailable()
            //
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
                _logInResult.emit(useCase(_idQuery.value, _passwordQuery.value).first())
            } catch (e: Exception) {
                _logInResult.emit(LogInResult.Failure(e.message.toString()))
            }
        }
    }
}
