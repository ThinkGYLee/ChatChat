package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import com.gyleedev.chatchat.domain.MessageData
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ResendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long) {
        repository.insertMessageToRemote(messageData).first()
    }
}
