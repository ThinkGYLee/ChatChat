package com.gyleedev.chatchat.ui.login

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")
    val idQuery: StateFlow<String> = _idQuery

    private val _passwordQuery = MutableStateFlow("")
    val passwordQuery: StateFlow<String> = _passwordQuery

    fun editId(id: String) {
        viewModelScope.launch {
            _idQuery.emit(id)
        }
    }

    fun editPassword(password: String) {
        viewModelScope.launch {
            _passwordQuery.emit(password)
        }
    }

    fun logInButtonClick(
        id: String,
        password: String
    ) {
        println("id : $id / password : $password")
    }
}
