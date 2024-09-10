package com.gyleedev.chatchat.ui.login

import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")
    val idQuery: StateFlow<String> = _idQuery

    private val _passwordQuery = MutableStateFlow("")
    val passwordQuery: StateFlow<String> = _passwordQuery

    fun logInButtonClick(
        id: String,
        password: String
    ) {
        println("id : $id / password : $password")
    }
}
