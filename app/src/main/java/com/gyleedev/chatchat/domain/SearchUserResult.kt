package com.gyleedev.chatchat.domain

sealed interface SearchUserResult {
    data class Success(
        val user: UserData
    ) : SearchUserResult
    data class Failure(
        val message: String
    ) : SearchUserResult
}
