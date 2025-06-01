package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData) =
        repository.deleteMessageRequest(messageData)
}
