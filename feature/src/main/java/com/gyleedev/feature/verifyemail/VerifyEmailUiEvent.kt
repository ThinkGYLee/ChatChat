package com.gyleedev.feature.verifyemail

sealed interface VerifyEmailEvent {
    data object Success: VerifyEmailEvent
    data object Fail: VerifyEmailEvent
    data object Cancel: VerifyEmailEvent
}