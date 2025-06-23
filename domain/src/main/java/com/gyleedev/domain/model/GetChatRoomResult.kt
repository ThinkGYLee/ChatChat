package com.gyleedev.domain.model

sealed interface GetChatRoomResult {
    data class Success(
        val chatRoom: ChatRoomData
    ) : GetChatRoomResult
    data object Failure : GetChatRoomResult
}
