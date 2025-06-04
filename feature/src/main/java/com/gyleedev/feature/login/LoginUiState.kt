package com.gyleedev.feature.login

sealed interface LoginUiState {
    data object Loading : LoginUiState
    data class Success(
        val idQuery: String,
        val passwordQuery: String,
        val idIsAvailable: Boolean,
        val passwordIsAvailable: Boolean,
        val loginIsAvailable: Boolean
    ) : LoginUiState
}
