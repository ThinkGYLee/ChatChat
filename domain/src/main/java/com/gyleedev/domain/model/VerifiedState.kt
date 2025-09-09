package com.gyleedev.domain.model

enum class VerifiedState {
    VERIFIED,
    INPROGRESS,
    NOTINPROGRESS,
    LOADING,
}

fun convertStringToVerifiedState(str: String): VerifiedState = try {
    VerifiedState.valueOf(str)
} catch (e: IllegalArgumentException) {
    VerifiedState.LOADING
}
