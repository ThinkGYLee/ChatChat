package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import com.gyleedev.chatchat.domain.MessageData
import javax.inject.Inject

class GetMessageFromLocalUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(messageData: MessageData) =
        repository.getMessage(messageData)
}
