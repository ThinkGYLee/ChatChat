package com.gyleedev.data.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.gyleedev.domain.model.VerifiedState
import com.gyleedev.domain.model.convertStringToVerifiedState
import dagger.hilt.android.qualifiers.ApplicationContext

interface AuthenticationPreference {
    fun getPassword(): String
    fun setPassword(password: String)
    fun getState(): VerifiedState
    fun setState(state: VerifiedState)
}

class AuthenticationPreferenceImpl(@ApplicationContext context: Context) : AuthenticationPreference {

    private val authenticationPreference: SharedPreferences =
        context.getSharedPreferences("Authentication", Context.MODE_PRIVATE)

    override fun getPassword(): String = authenticationPreference.getString("Password", "default_password") as String

    override fun setPassword(password: String) {
        authenticationPreference.edit { putString("Password", password) }
    }

    override fun getState(): VerifiedState {
        val stateString = authenticationPreference.getString("State", "NOTINPROGRESS") as String
        return convertStringToVerifiedState(stateString)
    }

    override fun setState(state: VerifiedState) {
        authenticationPreference.edit { putString("State", state.toString()) }
    }
}
