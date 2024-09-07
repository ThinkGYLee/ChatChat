package com.gyleedev.chatchat.ui.signin

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor() : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")
    val idQuery: StateFlow<String> = _idQuery

    private val _passwordQuery = MutableStateFlow("")
    val passwordQuery: StateFlow<String> = _passwordQuery

    private val _passwordCheckQuery = MutableStateFlow("")
    val passwordCheckQuery: StateFlow<String> = _passwordCheckQuery

    private val _signInIsAvailable = MutableStateFlow(false)
    val signInIsAvailable: StateFlow<Boolean> = _signInIsAvailable

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

    fun editPasswordCheck(passwordCheck: String) {
        viewModelScope.launch {
            _passwordCheckQuery.emit(passwordCheck)
            checkSignInAvailable()
        }
    }

    private fun checkSignInAvailable() {
        viewModelScope.launch {
            val checkId = idQuery.value.isNotEmpty()
            val checkPasswordLength = passwordQuery.value.length > 8
            val checkPasswordCheckLength = passwordCheckQuery.value.length > 8
            val checkPasswordSame = passwordQuery.value == passwordCheckQuery.value
            if (checkId && checkPasswordLength && checkPasswordCheckLength && checkPasswordSame) {
                _signInIsAvailable.emit(true)
            } else {
                _signInIsAvailable.emit(false)
            }
        }

    }

    fun logInButtonClick(
        id: String,
        password: String
    ) {
        println("id : $id / password : $password")
    }
}
