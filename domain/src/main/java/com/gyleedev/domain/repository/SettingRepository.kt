package com.gyleedev.domain.repository

interface SettingRepository {
    fun getTheme(): Int
    fun setTheme(mode: Int)
}
