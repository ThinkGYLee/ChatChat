package com.gyleedev.chatchat.ui.login

import android.app.Instrumentation
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : BaseViewModel() {

    private val _isFailState = mutableStateOf(false)
    val isFailState: State<Boolean> = _isFailState

    fun login(
        activityResult: Instrumentation.ActivityResult,
        onSuccess: () -> Unit
    ) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(activityResult.resultData)
                .getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) onSuccess()
                }
        } catch (e: Exception) {
            _isFailState.value = true
        }
    }
}
