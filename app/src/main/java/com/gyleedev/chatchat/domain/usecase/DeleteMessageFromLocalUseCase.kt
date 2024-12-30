package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import javax.inject.Inject

class DeleteMessageFromLocalUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageId: Long) =
        repository.deleteMessage(messageId)
}
