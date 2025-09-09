package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(messageData: MessageData) = repository.deleteMessageRequest(messageData)
}
