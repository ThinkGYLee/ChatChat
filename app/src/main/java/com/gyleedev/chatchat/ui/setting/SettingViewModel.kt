package com.gyleedev.chatchat.ui.setting

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.usecase.LogoutProcessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val logoutProcessUseCase: LogoutProcessUseCase
) : BaseViewModel() {
    private val _items = MutableStateFlow<List<SettingItems>>(emptyList<SettingItems>())
    val items: StateFlow<List<SettingItems>> = _items

    init {
        viewModelScope.launch {
            _items.emit(settingItemList)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutProcessUseCase()
        }
    }

    companion object {
        val settingItemList = listOf<SettingItems>(
            SettingItems.Header(title = R.string.setting_personal_info_header),
            SettingItems.Item(
                title = R.string.setting_account_manage_item, Icons.Default.ManageAccounts,
                SettingEvent.ACCOUNT
            ),
            SettingItems.Item(
                title = R.string.setting_my_info_item, Icons.Default.AccountBox,
                SettingEvent.MYINFORMATION
            ),
            SettingItems.Header(title = R.string.setting_general_setting_header),
            SettingItems.Item(
                title = R.string.setting_language_item, Icons.Default.Language,
                SettingEvent.LANGUAGE
            ),
            SettingItems.Item(
                title = R.string.setting_theme_item, Icons.Default.DarkMode,
                SettingEvent.THEME
            ),
            SettingItems.Header(title = R.string.setting_data_manage_header),
            SettingItems.Item(
                title = R.string.setting_chat_item, Icons.Default.ChatBubbleOutline,
                SettingEvent.CHAT
            )
        )
    }
}
