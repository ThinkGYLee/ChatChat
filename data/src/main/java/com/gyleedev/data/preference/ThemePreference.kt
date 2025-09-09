package com.gyleedev.data.preference

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
interface ThemePreference {
    fun getTheme(): Int
    fun setTheme(mode: Int)
}

class ThemePreferenceImpl(@ApplicationContext context: Context) : ThemePreference {

    private val themePreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    override fun getTheme(): Int = themePreferences.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    override fun setTheme(mode: Int) {
        themePreferences.edit { putInt("selectedTheme", mode) }
    }
}
