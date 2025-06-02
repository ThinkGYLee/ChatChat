package com.gyleedev.feature.chatroom

import com.gyleedev.domain.model.UserRelationState

sealed interface ChatRoomUiState {

    data object Loading : ChatRoomUiState

    data class Success(
        val userName: String,
        val uid: String,
        val relationState: UserRelationState
    ) : ChatRoomUiState
}
