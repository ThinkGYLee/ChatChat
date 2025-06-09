package com.gyleedev.feature.verifyemail

sealed interface VerifyEmailUiEvent {
    data object Success : VerifyEmailUiEvent
    data object Fail : VerifyEmailUiEvent
    data object Cancel : VerifyEmailUiEvent
}
