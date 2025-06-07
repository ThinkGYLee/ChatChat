package com.gyleedev.domain.model

enum class VerifiedState {
    VERIFIED,
    INPROGRESS,
    NOTINPROGRESS
}

fun convertStringToVerifiedState(str: String): VerifiedState {
    return try {
        VerifiedState.valueOf(str)
    } catch (e: IllegalArgumentException) {
        VerifiedState.NOTINPROGRESS
    }
}
