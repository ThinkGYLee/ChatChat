package com.gyleedev.data.repository

import com.gyleedev.data.preference.ThemePreference
import com.gyleedev.domain.repository.SettingRepository
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(
    private val preference: ThemePreference,
) : SettingRepository {
    override fun getTheme(): Int = preference.getTheme()

    override fun setTheme(mode: Int) {
        preference.setTheme(mode)
    }
}
