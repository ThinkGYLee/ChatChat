package com.gyleedev.data.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.gyleedev.domain.model.UserData
import dagger.hilt.android.qualifiers.ApplicationContext

interface PasswordPreference {
    fun getPassword(): String
    fun setPassword(password: String)
}

class PasswordPreferenceImpl(@ApplicationContext context: Context) : PasswordPreference {

    private val passwordPreference: SharedPreferences =
        context.getSharedPreferences("Password", Context.MODE_PRIVATE)

    override fun getPassword(): String {
        return passwordPreference.getString("Password", "default_password") as String
    }

    override fun setPassword(password: String) {
        passwordPreference.edit { putString("Password", password) }
    }
}
