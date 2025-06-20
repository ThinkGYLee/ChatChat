package com.gyleedev.domain.model

class ChatCreationException(
    val problemState: ChatCreationState,
    val restartState: ChatCreationState,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
