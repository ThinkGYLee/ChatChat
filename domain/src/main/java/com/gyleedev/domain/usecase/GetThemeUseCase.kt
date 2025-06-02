package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.SettingRepository
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val settingRepository: SettingRepository
) {
    operator fun invoke(): Int {
        return settingRepository.getTheme()
    }
}
