package com.gyleedev.feature.signin

sealed interface SigninUiState {
    data object Loading : SigninUiState
    data class Success(
        val idQuery: String,
        val nicknameQuery: String,
        val passwordQuery: String,
        val passwordCheckQuery: String,
        val idIsAvailable: Boolean,
        val nicknameIsAvailable: Boolean,
        val passwordIsAvailable: Boolean,
        val passwordCheckIsAvailable: Boolean,
        val passwordIsSame: Boolean,
        val signinIsAvailable: Boolean,
    ) : SigninUiState
}
