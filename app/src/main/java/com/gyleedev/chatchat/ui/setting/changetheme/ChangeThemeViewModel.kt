package com.gyleedev.chatchat.ui.setting.changetheme

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.util.PreferenceUtil
import com.gyleedev.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeThemeViewModel @Inject constructor(
    private val preferenceUtil: PreferenceUtil
) : BaseViewModel() {

    private val _currentTheme = MutableStateFlow<Int>(getTheme())
    val currentTheme: StateFlow<Int> = _currentTheme

    private fun getTheme(): Int {
        return preferenceUtil.getTheme()
    }

    fun setTheme(mode: Int) {
        viewModelScope.launch {
            preferenceUtil.setTheme(mode)
            _currentTheme.emit(mode)
        }
    }
}
