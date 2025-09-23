package com.gyleedev.feature.verifyemail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.UserData
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.P)
@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val getMyDataFromPreferenceUseCase: GetMyDataFromPreferenceUseCase,
    private val verifyEmailRequestUseCase: VerifyEmailRequestUseCase,
    private val getVerifiedStateUseCase: GetVerifiedStateUseCase,
    private val setVerifiedStateUseCase: SetVerifiedStateUseCase,
    private val checkVerifiedUseCase: CheckVerifiedUseCase,
    private val cancelSigninUseCase: CancelSigninUseCase,
    private val updateMyUserInformationUseCase: UpdateMyInfoUseCase,
) : BaseViewModel() {

    private val _uiEvent = MutableSharedFlow<VerifyEmailUiEvent>()
    val uiEvent: SharedFlow<VerifyEmailUiEvent> = _uiEvent

    @RequiresApi(Build.VERSION_CODES.P)
    private val userData = MutableStateFlow<UserData>(UserData())
    private val verifiedState = MutableStateFlow<VerifiedState>(
        VerifiedState.LOADING,
    )

    @RequiresApi(Build.VERSION_CODES.P)
    val uiState = combine(
        userData,
        verifiedState,
    ) { userData, verifiedState ->
        VerifyEmailUiState.Success(
            userData = userData,
            verifiedState = verifiedState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VerifyEmailUiState.Loading,
    )

    init {
        viewModelScope.launch {
            userData.emit(getMyDataFromPreferenceUseCase())
            verifiedState.emit(getVerifiedStateUseCase())
        }
    }

    fun cancelSignin() {
        viewModelScope.launch {
            val result = cancelSigninUseCase()
            if (result) {
                _uiEvent.emit(VerifyEmailUiEvent.Cancel)
            }
        }
    }

    fun verifyRequest() {
        viewModelScope.launch {
            val result = verifyEmailRequestUseCase()
            if (result) {
                setVerifiedStateUseCase(VerifiedState.INPROGRESS)
                userData.emit((uiState.value as VerifyEmailUiState.Success).userData)
                verifiedState.emit(VerifiedState.INPROGRESS)
            } else {
                _uiEvent.emit(VerifyEmailUiEvent.Fail)
            }
        }
    }

    fun checkVerified() {
        viewModelScope.launch {
            val result = checkVerifiedUseCase()
            if (result) {
                val updateResult = updateMyUserInformationUseCase(
                    (uiState.value as VerifyEmailUiState.Success).userData.copy(verified = true),
                )
                if (updateResult) {
                    setVerifiedStateUseCase(VerifiedState.VERIFIED)
                    _uiEvent.emit(VerifyEmailUiEvent.Success)
                } else {
                    _uiEvent.emit(VerifyEmailUiEvent.Fail)
                }
            } else {
                _uiEvent.emit(VerifyEmailUiEvent.Fail)
            }
        }
    }
}
