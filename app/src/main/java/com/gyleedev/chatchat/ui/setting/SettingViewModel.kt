package com.gyleedev.chatchat.ui.setting

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.usecase.LogoutProcessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val logoutProcessUseCase: LogoutProcessUseCase
) : BaseViewModel() {
    fun logout() {
        viewModelScope.launch {
            logoutProcessUseCase()
        }
    }
}
