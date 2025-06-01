package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class UpdateMessageStateUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageId: Long, rid: Long, message: MessageData) =
        repository.updateMessageState(messageId, rid, message)
}
