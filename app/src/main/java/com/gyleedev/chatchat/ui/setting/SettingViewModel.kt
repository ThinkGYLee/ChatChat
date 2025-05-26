package com.gyleedev.chatchat.ui.setting

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.domain.usecase.LogoutProcessUseCase
import com.gyleedev.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val logoutProcessUseCase: LogoutProcessUseCase
) : BaseViewModel() {

    private val _keys = MutableStateFlow<List<SettingKey>>(emptyList<SettingKey>())
    val keys: StateFlow<List<SettingKey>> = _keys

    init {
        viewModelScope.launch {
            _keys.emit(keyList)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutProcessUseCase()
        }
    }

    companion object {
        val keyList = listOf<SettingKey>(
            SettingKey.PERSONALINFO,
            SettingKey.ACCOUNT,
            SettingKey.MYINFORMATION,
            SettingKey.GENERALSETTING,
            SettingKey.LANGUAGE,
            SettingKey.THEME,
            SettingKey.DATAMANAGE,
            SettingKey.CHAT
        )
    }
}
