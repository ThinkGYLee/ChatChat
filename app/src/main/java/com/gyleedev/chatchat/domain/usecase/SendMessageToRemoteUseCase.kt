package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class SendMessageToRemoteUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(messageData: MessageData) =
        repository.insertMessageToRemote(messageData)
}
