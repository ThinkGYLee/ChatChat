package com.gyleedev.chatchat.ui.login

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
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
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

    private fun checkSignInAvailable() {
        viewModelScope.launch {
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            _idIsAvailable.emit(pattern.matcher(_idQuery.value).matches())
            _passwordIsAvailable.emit(_passwordQuery.value.length >= 8)
            _logInIsAvailable.emit(_idIsAvailable.value && _passwordIsAvailable.value)
        }
    }

    fun logInButtonClick() {
        auth.signInWithEmailAndPassword(idQuery.value, passwordQuery.value)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    println("success")
                    val user = auth.currentUser
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    println("fail : ${task.exception}")
                    /*Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)*/
                }
            }
        println("id : ${idQuery.value} / password : ${passwordQuery.value}")
    }
}
