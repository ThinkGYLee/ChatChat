package com.gyleedev.chatchat.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
        myDataPreference.edit { putString("Email", user.email) }
        myDataPreference.edit { putString("Name", user.name) }
        myDataPreference.edit { putString("Uid", user.uid) }
        myDataPreference.edit { putString("Picture", user.picture) }
        myDataPreference.edit { putString("Status", user.status) }
    }
}
