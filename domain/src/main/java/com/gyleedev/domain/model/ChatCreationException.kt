package com.gyleedev.domain.model

class ChatCreationException(
    val state: ChatCreationState,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
