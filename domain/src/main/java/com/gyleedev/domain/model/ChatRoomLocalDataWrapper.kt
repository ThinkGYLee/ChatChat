package com.gyleedev.domain.model

sealed interface ChatRoomLocalDataWrapper {
    data object Failure : ChatRoomLocalDataWrapper
    data class Success(
        val data: ChatRoomLocalData
    ) : ChatRoomLocalDataWrapper
}
