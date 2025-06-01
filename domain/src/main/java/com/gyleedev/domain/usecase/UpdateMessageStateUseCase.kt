package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class UpdateMessageStateUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageId: Long, rid: Long, message: MessageData) =
        repository.updateMessageState(messageId, rid, message)
}
