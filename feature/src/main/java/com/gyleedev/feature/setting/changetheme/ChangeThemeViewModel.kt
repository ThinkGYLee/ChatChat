package com.gyleedev.feature.setting.changetheme

import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.usecase.GetThemeUseCase
import com.gyleedev.domain.usecase.SetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeThemeViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
) : BaseViewModel() {

    private val _currentTheme = MutableStateFlow<Int>(getTheme())
    val currentTheme: StateFlow<Int> = _currentTheme

    private fun getTheme(): Int = getThemeUseCase()

    fun setTheme(mode: Int) {
        viewModelScope.launch {
            setThemeUseCase(mode)
            _currentTheme.emit(mode)
        }
    }
}
