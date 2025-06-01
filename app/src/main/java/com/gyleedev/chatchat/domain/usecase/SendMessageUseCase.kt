package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long, networkState: Boolean) {
        withContext(Dispatchers.IO) {
            repository.sendMessage(messageData, rid, networkState)
        }
    }
}
