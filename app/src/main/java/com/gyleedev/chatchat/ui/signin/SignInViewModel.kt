package com.gyleedev.chatchat.ui.signin

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.SignInResult
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.SignInAuthUseCase
import com.gyleedev.chatchat.domain.usecase.SignInDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInAuthUseCase: SignInAuthUseCase,
    private val signInDatabaseUseCase: SignInDatabaseUseCase
) : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")
    private val _nicknameQuery = MutableStateFlow("")
    private val _passwordQuery = MutableStateFlow("")
    private val _passwordCheckQuery = MutableStateFlow("")

    private val _idIsAvailable = MutableStateFlow(false)
    val idIsAvailable: StateFlow<Boolean> = _idIsAvailable
    private val _nicknameIsAvailable = MutableStateFlow(false)
    val nicknameIsAvailable: StateFlow<Boolean> = _nicknameIsAvailable
    private val _passwordIsAvailable = MutableStateFlow(false)
    val passwordIsAvailable: StateFlow<Boolean> = _passwordIsAvailable
    private val _passwordCheckIsAvailable = MutableStateFlow(false)
    private val _passwordIsSame = MutableStateFlow(false)
    val passwordIsSame: StateFlow<Boolean> = _passwordIsSame

    private val _signInIsAvailable = MutableStateFlow(false)
    val signInIsAvailable: StateFlow<Boolean> = _signInIsAvailable

    private val _signInProgress = MutableSharedFlow<SignInResult>()
    val signInProgress: SharedFlow<SignInResult> = _signInProgress

    fun editId(id: String) {
        viewModelScope.launch {
            _idQuery.emit(id)
            checkSignInAvailable()
        }
    }

    fun editNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameQuery.emit(nickname)
            checkSignInAvailable()
        }
    }

    fun editPassword(password: String) {
        viewModelScope.launch {
            _passwordQuery.emit(password)
            checkSignInAvailable()
        }
    }

    fun editPasswordCheck(passwordCheck: String) {
        viewModelScope.launch {
            _passwordCheckQuery.emit(passwordCheck)
            checkSignInAvailable()
        }
    }

    private fun checkSignInAvailable() {
        viewModelScope.launch {
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            _idIsAvailable.emit(pattern.matcher(_idQuery.value).matches())
            _nicknameIsAvailable.emit(_nicknameQuery.value.length >= 2)
            _passwordIsAvailable.emit(_passwordQuery.value.length >= 8)
            _passwordCheckIsAvailable.emit(_passwordCheckQuery.value.length >= 8)
            _passwordIsSame.emit(_passwordQuery.value == _passwordCheckQuery.value)
            _signInIsAvailable.emit(_idIsAvailable.value && _passwordIsAvailable.value && _passwordCheckIsAvailable.value && _passwordIsSame.value)
        }
    }

    fun signInRequest() {
        viewModelScope.launch {
            val process =
                signInAuthUseCase(
                    id = _idQuery.value,
                    password = _passwordQuery.value,
                    nickname = _nicknameQuery.value
                ).first()

            if (process != null) {
                signInDatabase(process)
            } else {
                _signInProgress.emit(SignInResult.Failure)
            }
        }
    }

    private fun signInDatabase(userData: UserData) {
        viewModelScope.launch {
            val process = signInDatabaseUseCase(userData).first()
            _signInProgress.emit(process)
        }
    }
}
