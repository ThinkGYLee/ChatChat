package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class DeleteMessageFromLocalUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageId: Long) =
        repository.deleteLocalMessage(messageId)
}
