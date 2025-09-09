package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageFromLocalUseCase @Inject constructor(
    private val repository: MessageRepository,
) {
    operator fun invoke(messageData: MessageData) = repository.getMessage(messageData)
}
