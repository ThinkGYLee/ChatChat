package com.gyleedev.chatchat.ui.signin

import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor() : BaseViewModel() {

    fun logInButtonClick(
        id: String,
        password: String
    ) {
        println("id : $id / password : $password")
    }
}
