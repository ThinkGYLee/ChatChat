package com.gyleedev.feature.verifyemail

import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.usecase.CancelSigninUseCase
import com.gyleedev.domain.usecase.GetMyDataFromPreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val getMyDataFromPreferenceUseCase: GetMyDataFromPreferenceUseCase,
    private val cancelSigninUseCase: CancelSigninUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<VerifyEmailUiState>(VerifyEmailUiState.Loading)
    val uiState: StateFlow<VerifyEmailUiState> = _uiState

    private val _cancelSigninResult = MutableSharedFlow<Boolean>()
    val cancelSigninResult: SharedFlow<Boolean> = _cancelSigninResult


    init {
        viewModelScope.launch {
            _uiState.emit(
                VerifyEmailUiState.Success(
                    userData = getMyDataFromPreferenceUseCase()
                )
            )
        }
    }

    fun cancelSignin() {
        viewModelScope.launch {
            val result = cancelSigninUseCase()
            _cancelSigninResult.emit(result)
        }
    }
}