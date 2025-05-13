package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import com.gyleedev.chatchat.domain.MessageData
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long, networkState: Boolean) {
        repository.sendMessage(messageData, rid, networkState)
    }
}
