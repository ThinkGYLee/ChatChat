package com.gyleedev.domain.model

class GetChatRoomException(
    val problemState: GetChatRoomState,
    val restartState: GetChatRoomState,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
