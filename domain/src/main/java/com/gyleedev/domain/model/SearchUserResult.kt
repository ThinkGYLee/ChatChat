package com.gyleedev.domain.model

sealed interface SearchUserResult {
    data class Success(
        val user: UserData
    ) : SearchUserResult
    data class Failure(
        val message: String
    ) : SearchUserResult
}
