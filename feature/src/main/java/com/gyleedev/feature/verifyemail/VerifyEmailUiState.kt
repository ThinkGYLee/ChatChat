package com.gyleedev.feature.verifyemail

import com.gyleedev.domain.model.UserData

sealed interface VerifyEmailUiState {
    data object Loading: VerifyEmailUiState
    data class Success(
        val userData: UserData
    ): VerifyEmailUiState
}