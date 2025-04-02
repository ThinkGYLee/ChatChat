package com.gyleedev.chatchat.ui.setting

import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel() {

    fun logout() {
        logoutUseCase
    }
}
