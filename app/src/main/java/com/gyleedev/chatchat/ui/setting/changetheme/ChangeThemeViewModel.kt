package com.gyleedev.chatchat.ui.setting.changetheme

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.data.preference.ThemePreference
import com.gyleedev.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeThemeViewModel @Inject constructor(
    private val themePreference: ThemePreference
) : BaseViewModel() {

    private val _currentTheme = MutableStateFlow<Int>(getTheme())
    val currentTheme: StateFlow<Int> = _currentTheme

    private fun getTheme(): Int {
        return themePreference.getTheme()
    }

    fun setTheme(mode: Int) {
        viewModelScope.launch {
            this@ChangeThemeViewModel.themePreference.setTheme(mode)
            _currentTheme.emit(mode)
        }
    }
}
