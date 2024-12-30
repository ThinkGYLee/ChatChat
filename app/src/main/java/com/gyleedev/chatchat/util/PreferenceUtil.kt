package com.gyleedev.chatchat.util

import android.content.Context
import android.content.SharedPreferences
import com.gyleedev.chatchat.domain.UserData
import dagger.hilt.android.qualifiers.ApplicationContext

class PreferenceUtil(@ApplicationContext context: Context) {

    private val myDataPreference: SharedPreferences =
        context.getSharedPreferences("MyData", Context.MODE_PRIVATE)

    fun getMyData(): UserData {
        return UserData(
            email = myDataPreference.getString("Email", "default email") as String,
            uid = myDataPreference.getString("Uid", "default uid") as String,
            name = myDataPreference.getString("Name", "default name") as String,
            picture = myDataPreference.getString("Picture", "default picture") as String,
            status = myDataPreference.getString("Status", "default status") as String
        )
    }
    fun setMyData(user: UserData) {
        myDataPreference.edit().putString("Email", user.email).apply()
        myDataPreference.edit().putString("Name", user.name).apply()
        myDataPreference.edit().putString("Uid", user.uid).apply()
        myDataPreference.edit().putString("Picture", user.picture).apply()
        myDataPreference.edit().putString("Status", user.status).apply()
    }

    fun isKeyExist(): Boolean {
        return myDataPreference.getString("Token", "").let {
            !it.isNullOrEmpty()
        }
    }

    fun deleteKey() {

    }
}