package com.gyleedev.chatchat.ui.chatroom

sealed interface ChatRoomUiState {

    data object Loading : ChatRoomUiState

    data class Success(
        val userName: String,
        val uid: String
    ) : ChatRoomUiState
}
