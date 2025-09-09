package com.gyleedev.feature.signin

import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.SignInResult
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.usecase.SignInAuthUseCase
import com.gyleedev.domain.usecase.SignInDatabaseUseCase
import com.gyleedev.util.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SigninViewModel @Inject constructor(
    private val signInAuthUseCase: SignInAuthUseCase,
    private val signInDatabaseUseCase: SignInDatabaseUseCase,
) : BaseViewModel() {

    private val idQuery = MutableStateFlow("")
    private val nicknameQuery = MutableStateFlow("")
    private val passwordQuery = MutableStateFlow("")
    private val passwordCheckQuery = MutableStateFlow("")
    private val idIsAvailable = MutableStateFlow(false)
    private val nicknameIsAvailable = MutableStateFlow(false)
    private val passwordIsAvailable = MutableStateFlow(false)
    private val passwordCheckIsAvailable = MutableStateFlow(false)
    private val passwordIsSame = MutableStateFlow(false)
    private val signInIsAvailable = MutableStateFlow(false)

    private val _signInProgress = MutableSharedFlow<SignInResult>()
    val signInProgress: SharedFlow<SignInResult> = _signInProgress

    val uiState = combine(
        idQuery,
        nicknameQuery,
        passwordQuery,
        passwordCheckQuery,
        idIsAvailable,
        nicknameIsAvailable,
        passwordIsAvailable,
        passwordCheckIsAvailable,
        passwordIsSame,
        signInIsAvailable,
    ) { idQuery, nicknameQuery, passwordQuery, passwordCheckQuery, idIsAvailable, nicknameIsAvailable, passwordIsAvailable, passwordCheckIsAvailable, passwordIsSame, signinIsAvailable ->
        SigninUiState.Success(
            idQuery = idQuery,
            nicknameQuery = nicknameQuery,
            passwordQuery = passwordQuery,
            passwordCheckQuery = passwordCheckQuery,
            idIsAvailable = idIsAvailable,
            nicknameIsAvailable = nicknameIsAvailable,
            passwordIsAvailable = passwordIsAvailable,
            passwordCheckIsAvailable = passwordCheckIsAvailable,
            passwordIsSame = passwordIsSame,
            signinIsAvailable = signinIsAvailable,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SigninUiState.Loading,
    )

    fun editId(id: String) {
        viewModelScope.launch {
            idQuery.emit(id)
            checkSignInAvailable()
        }
    }

    fun editNickname(nickname: String) {
        viewModelScope.launch {
            nicknameQuery.emit(nickname)
            checkSignInAvailable()
        }
    }

    fun editPassword(password: String) {
        viewModelScope.launch {
            passwordQuery.emit(password)
            checkSignInAvailable()
        }
    }

    fun editPasswordCheck(passwordCheck: String) {
        viewModelScope.launch {
            passwordCheckQuery.emit(passwordCheck)
            checkSignInAvailable()
        }
    }

    private fun checkSignInAvailable() {
        viewModelScope.launch {
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            idIsAvailable.emit(pattern.matcher(idQuery.value).matches())
            nicknameIsAvailable.emit(nicknameQuery.value.length >= 2)
            passwordIsAvailable.emit(passwordQuery.value.length >= 8)
            passwordCheckIsAvailable.emit(passwordCheckQuery.value.length >= 8)
            passwordIsSame.emit(passwordQuery.value == passwordCheckQuery.value)
            signInIsAvailable.emit(idIsAvailable.value && passwordIsAvailable.value && passwordCheckIsAvailable.value && passwordIsSame.value)
        }
    }

    fun signInRequest() {
        viewModelScope.launch {
            val process =
                signInAuthUseCase(
                    id = idQuery.value,
                    password = passwordQuery.value,
                    nickname = nicknameQuery.value,
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
