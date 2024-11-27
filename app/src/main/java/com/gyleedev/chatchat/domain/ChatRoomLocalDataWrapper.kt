package com.gyleedev.chatchat.domain

sealed interface ChatRoomLocalDataWrapper {
    data object Failure : ChatRoomLocalDataWrapper
    data class Success(
        val data: ChatRoomLocalData
    ) : ChatRoomLocalDataWrapper
}
