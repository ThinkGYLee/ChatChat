package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.SettingRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val settingRepository: SettingRepository
) {
    operator fun invoke(mode: Int) {
        settingRepository.setTheme(mode)
    }
}
