package com.gyleedev.chatchat.ui.signin

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : BaseViewModel() {

    private val _idQuery = MutableStateFlow("")
    private val _passwordQuery = MutableStateFlow("")
    private val _passwordCheckQuery = MutableStateFlow("")

    private val _idIsAvailable = MutableStateFlow(false)
    val idIsAvailable: StateFlow<Boolean> = _idIsAvailable
    private val _passwordIsAvailable = MutableStateFlow(false)
    val passwordIsAvailable: StateFlow<Boolean> = _passwordIsAvailable
    private val _passwordCheckIsAvailable = MutableStateFlow(false)
    private val _passwordIsSame = MutableStateFlow(false)
    val passwordIsSame: StateFlow<Boolean> = _passwordIsSame

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
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            _idIsAvailable.emit(pattern.matcher(_idQuery.value).matches())
            _passwordIsAvailable.emit(_passwordQuery.value.length >= 8)
            _passwordCheckIsAvailable.emit(_passwordCheckQuery.value.length >= 8)
            _passwordIsSame.emit(_passwordQuery.value == _passwordCheckQuery.value)
            _signInIsAvailable.emit(_idIsAvailable.value && _passwordIsAvailable.value && _passwordCheckIsAvailable.value && _passwordIsSame.value)
        }
    }

    fun signInRequest() {
        auth.createUserWithEmailAndPassword(_idQuery.value, _passwordQuery.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    println(user)
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)

                    println("Authentication failed. :${task.exception?.message}")
                    // updateUI(null)
                }
            }
    }
}
