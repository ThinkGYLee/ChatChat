package com.gyleedev.feature.verifyemail

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.model.VerifiedState

sealed interface VerifyEmailUiState {
    data object Loading : VerifyEmailUiState
    data class Success(
        val userData: UserData,
        val verifiedState: VerifiedState
    ) : VerifyEmailUiState
}
