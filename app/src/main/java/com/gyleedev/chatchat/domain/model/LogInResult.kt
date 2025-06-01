package com.gyleedev.chatchat.domain.model

sealed interface LogInResult {
    data object Success : LogInResult
    data class Failure(
        val message: String
    ) : LogInResult
}
