package com.gyleedev.feature.verifyemail

import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.VerifiedState
import com.gyleedev.domain.usecase.CancelSigninUseCase
import com.gyleedev.domain.usecase.CheckVerifiedUseCase
import com.gyleedev.domain.usecase.GetMyDataFromPreferenceUseCase
import com.gyleedev.domain.usecase.GetVerifiedStateUseCase
import com.gyleedev.domain.usecase.SetVerifiedStateUseCase
import com.gyleedev.domain.usecase.UpdateMyInfoUseCase
import com.gyleedev.domain.usecase.VerifyEmailRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val getMyDataFromPreferenceUseCase: GetMyDataFromPreferenceUseCase,
    private val verifyEmailRequestUseCase: VerifyEmailRequestUseCase,
    private val getVerifiedStateUseCase: GetVerifiedStateUseCase,
    private val setVerifiedStateUseCase: SetVerifiedStateUseCase,
    private val checkVerifiedUseCase: CheckVerifiedUseCase,
    private val cancelSigninUseCase: CancelSigninUseCase,
    private val updateMyUserInformationUseCase: UpdateMyInfoUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<VerifyEmailUiState>(VerifyEmailUiState.Loading)
    val uiState: StateFlow<VerifyEmailUiState> = _uiState

    private val _cancelSigninResult = MutableSharedFlow<Boolean>()
    val cancelSigninResult: SharedFlow<Boolean> = _cancelSigninResult

    private val _verifyCheckResult = MutableSharedFlow<Boolean>()
    val verifyCheckResult: SharedFlow<Boolean> = _verifyCheckResult

    init {
        viewModelScope.launch {
            _uiState.emit(
                VerifyEmailUiState.Success(
                    userData = getMyDataFromPreferenceUseCase(),
                    verifiedState = getVerifiedStateUseCase()
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

    fun verifyRequest() {
        viewModelScope.launch {
            val result = verifyEmailRequestUseCase()
            if (result) {
                setVerifiedStateUseCase(VerifiedState.INPROGRESS)
                _uiState.emit(
                    VerifyEmailUiState.Success(
                        userData = (_uiState.value as VerifyEmailUiState.Success).userData,
                        verifiedState = VerifiedState.INPROGRESS
                    )
                )
            } else {
                _verifyCheckResult.emit(false)
            }
        }
    }

    fun checkVerified() {
        viewModelScope.launch {
            val result = checkVerifiedUseCase()
            if (result) {
                val updateResult = updateMyUserInformationUseCase(
                    (_uiState.value as VerifyEmailUiState.Success).userData.copy(verified = true)
                ).first()
                if (updateResult) {
                    setVerifiedStateUseCase(VerifiedState.VERIFIED)
                    _verifyCheckResult.emit(true)
                } else {
                    _verifyCheckResult.emit(false)
                }
            } else {
                _verifyCheckResult.emit(false)
            }
        }
    }
}
