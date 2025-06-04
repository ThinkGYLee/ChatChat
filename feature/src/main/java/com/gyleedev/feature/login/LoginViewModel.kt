package com.gyleedev.feature.login

import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.LogInState
import com.gyleedev.domain.usecase.LoginProcessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val useCase: LoginProcessUseCase
) : BaseViewModel() {

    private val idQuery = MutableStateFlow("")
    private val passwordQuery = MutableStateFlow("")
    private val idIsAvailable = MutableStateFlow(false)
    private val passwordIsAvailable = MutableStateFlow(false)
    private val logInIsAvailable = MutableStateFlow(false)
    private val _logInResult = MutableSharedFlow<LogInState>()
    val logInResult: SharedFlow<LogInState> = _logInResult

    val uiState = combine(
        idQuery,
        passwordQuery,
        idIsAvailable,
        passwordIsAvailable,
        logInIsAvailable
    ) { idQuery, passwordQuery, idIsAvailable, passwordIsAvailable, loginIsAvailable ->
        LoginUiState.Success(
            idQuery = idQuery,
            passwordQuery = passwordQuery,
            idIsAvailable = idIsAvailable,
            passwordIsAvailable = passwordIsAvailable,
            loginIsAvailable = loginIsAvailable
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginUiState.Loading
    )

    fun editId(id: String) {
        viewModelScope.launch {
            idQuery.emit(id)
            checkSignInAvailable()
        }
    }

    fun editPassword(password: String) {
        viewModelScope.launch {
            passwordQuery.emit(password)
            checkSignInAvailable()
        }
    }

    private suspend fun checkSignInAvailable() {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        idIsAvailable.emit(pattern.matcher(idQuery.value).matches())
        passwordIsAvailable.emit(passwordQuery.value.length >= 8)
        logInIsAvailable.emit(idIsAvailable.value && passwordIsAvailable.value)
    }

    fun logInButtonClick() {
        viewModelScope.launch {
            try {
                _logInResult.emit(useCase(idQuery.value, passwordQuery.value))
            } catch (e: Exception) {
                _logInResult.emit(LogInState.Failure(e.message.toString()))
            }
        }
    }
}
