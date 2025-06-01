package com.gyleedev.chatchat.ui.chatroom

import com.gyleedev.chatchat.domain.model.UserRelationState

sealed interface ChatRoomUiState {

    data object Loading : ChatRoomUiState

    data class Success(
        val userName: String,
        val uid: String,
        val relationState: UserRelationState
    ) : ChatRoomUiState
}
