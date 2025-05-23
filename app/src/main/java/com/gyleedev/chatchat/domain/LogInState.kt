package com.gyleedev.chatchat.domain

sealed interface LogInState {
    data class Success(
        val userData: UserData
    ) : LogInState

    data class Failure(
        val message: String
    ) : LogInState
}
